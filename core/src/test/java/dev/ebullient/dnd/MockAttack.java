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
package dev.ebullient.dnd;

import dev.ebullient.dnd.combat.Attack;

public class MockAttack implements Attack {

    final String name;

    public Damage damage;
    public int attackModifier;
    public String savingThrow;

    public MockAttack(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Damage getDamage() {
        return damage;
    }

    @Override
    public int getAttackModifier() {
        return attackModifier;
    }

    @Override
    public String getSavingThrow() {
        return savingThrow;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(name).append("[");
        if (attackModifier != 0) {
            sb.append(attackModifier).append("hit,");
        }
        if (savingThrow != null) {
            sb.append(savingThrow).append(",");
        }

        sb.append(damage.toString().replaceAll("\\s+", ""))
                .append("]");
        return sb.toString();
    }
}