/*
 * Copyright © 2020 IBM Corp. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package dev.ebullient.dnd.combat;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.ebullient.dnd.beastiary.Beast;
import dev.ebullient.dnd.combat.Attack.Damage;
import dev.ebullient.dnd.mechanics.Ability;
import dev.ebullient.dnd.mechanics.Dice;

public class Encounter {
    static final Logger logger = LoggerFactory.getLogger(Encounter.class);
    final static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd-HHmm");

    final String id = LocalDateTime.now().format(formatter) + "-" + Integer.toHexString(this.hashCode());

    final TargetSelector selector;
    final Dice.Method method;
    final Set<Combatant> initiativeOrder;

    public Encounter(TargetSelector selector, Dice.Method method, List<Beast> beasts) {
        this.selector = selector;
        this.method = method;
        this.initiativeOrder = new TreeSet<>(Comparators.InitiativeOrder);
        for (Beast b : beasts) {
            this.initiativeOrder.add(new Combatant(b, method));
        }
    }

    Encounter(TargetSelector selector, Dice.Method method, Set<Combatant> combatants) {
        this.selector = selector;
        this.method = method;
        this.initiativeOrder = combatants;
    }

    public boolean isFinal() {
        return initiativeOrder.size() <= 1;
    }

    public RoundResult oneRound() {
        logger.debug("take turns: {}", initiativeOrder);

        RoundResult result = new RoundResult(initiativeOrder);
        for (Combatant attacker : initiativeOrder) {
            if (attacker.isAlive()) {
                Combatant target = selector.chooseTarget(attacker, initiativeOrder);

                // Single or many attacks
                List<Attack> attacks = attacker.getAttacks();
                for (Attack a : attacks) {
                    if (a == null) {
                        throw new IllegalStateException("Attack should not be null " + attacks);
                    }
                    if (target.isAlive()) {
                        AttackResult r = new AttackResult(attacker, target, a, method, id);
                        r.attack();
                        result.events.add(r);
                        logger.debug("take turns: {}", r);
                    }
                }

                // Highlander
                if (target.hitPoints <= 0) {
                    result.survivors.remove(target);
                }
            }
        }

        initiativeOrder.retainAll(result.survivors);

        logger.debug("take turns: survivors {}", result.survivors);
        return result;
    }

    public static class RoundResult {
        List<Combatant> survivors;
        List<AttackResult> events;

        RoundResult(Set<Combatant> initiativeOrder) {
            events = new ArrayList<>();
            survivors = new ArrayList<>(initiativeOrder);
        }

        public List<AttackResult> getEvents() {
            return events;
        }

        public List<Combatant> getSurvivors() {
            return survivors;
        }
    }

    public static class AttackResult {
        final Combatant attacker;
        final Combatant target;
        final Attack a;
        final String encounterId;

        boolean hit;
        boolean critical;
        boolean saved;
        Dice.Method method;
        int damage;

        AttackResult(Combatant attacker, Combatant target,
                Attack a, Dice.Method method, String encounterId) {
            this.attacker = attacker;
            this.target = target;
            this.a = a;
            this.method = method;
            this.encounterId = encounterId;
        }

        AttackResult attack() {
            if (a.getAttackModifier() != 0) {
                attemptAttack();
            } else if (a.getSavingThrow() != null) {
                makeAttackWithSavingThrow();
            } else {
                // We're still reading from some input somewhere.
                throw new IllegalArgumentException(attacker.getName() + " has badly formed attack " + a);
            }
            return this;
        }

        void attemptAttack() {
            // Did we hit?
            int attackRoll = Dice.d20();
            if (attackRoll == 1) {
                // critical fail. WOOPS!
                critical = true;
                hit = false;
            } else if (attackRoll == 20) {
                // critical hit! double damage!
                critical = true;
                hit = true;
            } else {
                critical = false;
                // Add attack modifier, then see if we hit. ;)
                attackRoll += a.getAttackModifier();
                int targetValue = target.getArmorClass();
                hit = attackRoll >= targetValue;
            }

            if (hit) {
                damage = Dice.roll(a.getDamage().getAmount(), method);
                if (critical) {
                    damage += damage;
                }
                target.takeDamage(damage); // ouch
                additionalEffects();
            }
        }

        void makeAttackWithSavingThrow() {
            hit = true;

            Matcher m = Attack.SAVE.matcher(a.getSavingThrow());
            if (m.matches()) {
                int dc = Integer.parseInt(m.group(2));
                int save = Dice.d20() + target.getSavingThrow(Ability.valueOf(m.group(1)));

                damage = Dice.roll(a.getDamage().getAmount(), method);

                if (save >= dc) {
                    saved = true;
                    damage = damage / 2;
                }

                target.takeDamage(damage); // ouch
            } else {
                throw new IllegalArgumentException(attacker.getName() + " has badly formed saving throw " + a);
            }
        }

        void additionalEffects() {
            // If the attack brings additional damage, and it hits..
            Damage effect = a.getAdditionalEffect();
            if (effect != null) {
                int effectDamage = 0;
                if (effect.getAmount() != null && !effect.getAmount().isEmpty()) {
                    effectDamage = Dice.roll(effect.getAmount(), method);
                }

                String savingThrow = effect.getSavingThrow();
                if (savingThrow != null) {
                    Matcher m = Attack.SAVE.matcher(savingThrow);
                    if (m.matches()) {
                        int dc = Integer.parseInt(m.group(2));
                        int save = Dice.d20() + target.getSavingThrow(Ability.valueOf(m.group(1)));

                        if ("hpdrain".equals(effect.getType())) {
                            if (save < dc) {
                                int maxhp = target.getMaxHitPoints();
                                if (damage > maxhp) {
                                    target.takeDamage(damage);

                                }
                            }
                        } else {
                            if (save >= dc) {
                                saved = true;
                                effectDamage = effectDamage / 2;
                            }
                            target.takeDamage(effectDamage); // ouch
                        }
                    }
                } else {
                    target.takeDamage(effectDamage); // ouch
                }
            }
        }

        public String toString() {
            String success = hit ? "hit>" : "miss:";
            success = critical ? success.toUpperCase(Locale.ROOT) : success;

            StringBuilder sb = new StringBuilder();
            sb.append(success).append(" ")
                    .append(attacker.getName()).append("(").append(attacker.getRelativeHealth()).append(")")
                    .append(" -> ")
                    .append(target.getName()).append("(").append(target.getRelativeHealth()).append(")");

            if (damage != 0) {
                sb.append(" for ").append(damage).append(" damage using ").append(a);
            }

            return sb.toString();
        }

        public boolean wasCritical() {
            return critical;
        }

        public boolean wasHit() {
            return hit;
        }

        public boolean wasSaved() {
            return saved;
        }

        public int getDamage() {
            return damage;
        }
    }

}
