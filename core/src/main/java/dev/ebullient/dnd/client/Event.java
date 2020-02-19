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
package dev.ebullient.dnd.client;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonIgnoreProperties(ignoreUnknown = true)
class Event implements dev.ebullient.dnd.combat.RoundResult.Event {

    @JsonDeserialize(as = dev.ebullient.dnd.client.Combatant.class)
    Combatant actor;

    @JsonDeserialize(as = dev.ebullient.dnd.client.Combatant.class)
    Combatant target;

    String name;
    String type;
    String attack;

    String actorStartingCondition;
    String targetStartingCondition;
    String actorEndingCondition;
    String targetEndingCondition;

    boolean hit;
    boolean critical;
    boolean saved;
    int damageAmount;

    int difficultyClass;
    int attackModifier;

    boolean effectSaved;
    int effectAmount;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Combatant getActor() {
        return actor;
    }

    public void setActor(Combatant actor) {
        this.actor = actor;
    }

    public Combatant getTarget() {
        return target;
    }

    public void setTarget(Combatant target) {
        this.target = target;
    }

    public String getActorStartingCondition() {
        return actorStartingCondition;
    }

    public void setActorStartingCondition(String actorStartingCondition) {
        this.actorStartingCondition = actorStartingCondition;
    }

    public String getTargetStartingCondition() {
        return targetStartingCondition;
    }

    public void setTargetStartingCondition(String targetStartingCondition) {
        this.targetStartingCondition = targetStartingCondition;
    }

    public String getActorEndingCondition() {
        return actorEndingCondition;
    }

    public void setActorEndingCondition(String actorEndingCondition) {
        this.actorEndingCondition = actorEndingCondition;
    }

    public String getTargetEndingCondition() {
        return targetEndingCondition;
    }

    public void setTargetEndingCondition(String targetEndingCondition) {
        this.targetEndingCondition = targetEndingCondition;
    }

    public boolean isHit() {
        return hit;
    }

    public void setHit(boolean hit) {
        this.hit = hit;
    }

    public boolean isCritical() {
        return critical;
    }

    public void setCritical(boolean critical) {
        this.critical = critical;
    }

    public boolean isSaved() {
        return saved;
    }

    public void setSaved(boolean saved) {
        this.saved = saved;
    }

    @JsonIgnore
    public String hitOrMiss() {
        return (critical ? "critical " : "")
                + (saved ? "saved " : "")
                + (hit ? "hit" : "miss");
    }

    public int getDamageAmount() {
        return damageAmount;
    }

    public void setDamageAmount(int damageAmount) {
        this.damageAmount = damageAmount;
    }

    public boolean isEffectSaved() {
        return effectSaved;
    }

    public void setEffectSaved(boolean effectSaved) {
        this.effectSaved = effectSaved;
    }

    public int getEffectAmount() {
        return effectAmount;
    }

    public void setEffectAmount(int effectAmount) {
        this.effectAmount = effectAmount;
    }

    public int getDifficultyClass() {
        return difficultyClass;
    }

    public void setDifficultyClass(int difficultyClass) {
        this.difficultyClass = difficultyClass;
    }

    public int getAttackModifier() {
        return attackModifier;
    }

    public void setAttackModifier(int attackModifier) {
        this.attackModifier = attackModifier;
    }
}
