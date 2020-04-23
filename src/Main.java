import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

class Entity  {
    public float x;
    public float y;
    public float tx = 0;
    public float ty = 0;
    public float fullness = 1f;
    public float maxSpeed = 0.5f;
    public float currentSpeed = maxSpeed;
    public float recoverySpeed = 0.5f;
    public boolean useMaxSpeed = false;
    public final float coefSpeedDecrease = 1f - (1f - recoverySpeed) / 1024f;
    public final float coefSpeedIncrease = maxSpeed - maxSpeed * (1f - recoverySpeed / 1024f);
    public float maxHealth = 0.5f;
    public float currentHealth = maxHealth;
    public float recoveryHealth = 0.5f;
    public final float coefHealthIncrease = maxHealth - maxHealth * (1f - recoveryHealth / 1024f);
    public float aggressiveness = 0.5f;
    public float toxicity = 0.5f;
    public float force = 0.5f;
    public int currentAge;
    public float maxAge = 0.5f;
    public boolean alive = true;
    public boolean estrus = false;
    public int currentEstrusDuration;
    public boolean pregnancy = false;
    public int currentPregnancyDuration;
    public Entity malePartner;

    public Entity (float x, float y) {
        this.x = x;
        this.y = y;
    }
}

public class Main {
    final int W = 800;
    final int H = 800;
    final float maxFullness = 3f;
    final float sightDistance = 128f;
    final float directionChangeRate = 0.001f;
    final float radiusEntity = 8f;
    final float chanceMutation = 1 / 8f;
    final float coefFullnessIncrease = 0.001f;
    final float coefFullnessExcess = 0.00003f / coefFullnessIncrease;
    final float coefFullnessChange = coefFullnessIncrease / 8 * (1 - coefFullnessExcess);
    final float coefMutation = 1 / 4f;
    int logicOnRenderingRate = 100;
    final int estrusDuration = 1024;
    final int pregnancyDuration = 1024;
    final int normalLifeSpan = 65536;
    float sumAggressiveness;
    float sumMaxSpeed;
    float sumForce;
    float sumToxicity;
    float sumMaxAge;
    float sumRecoverySpeed;
    float sumMaxHealth;
    float sumRecoveryHealth;
    ArrayList<Entity> entities = new ArrayList<>();
    int countCycles = 0;
    int countDead = 0;
    float maxDist = sightDistance * sightDistance;
    float maxDistPartner = radiusEntity * radiusEntity;
    int deathFromExhaustion = 0;
    int deathFromAge = 0;
    int deathByKilling = 0;
    int asexualReproduction = 0;
    int sexualReproduction = 0;

    public static void main(String[] args) {
        new Main();
    }

    public Main() {
        EventQueue.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ignored) {
            }
            JFrame frame = new JFrame();
            JPanel topPanel = new JPanel();
            JButton slow = new JButton("Slow");
            slow.addActionListener(e -> logicOnRenderingRate(1));
            JButton fast = new JButton("Fast");
            fast.addActionListener(e -> logicOnRenderingRate(1000));
            topPanel.add(slow);
            topPanel.add(fast);
            Container contentPane = frame.getContentPane();
            contentPane.add("North", topPanel);
            frame.add(new FormPane(), BorderLayout.CENTER);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    public void logicOnRenderingRate(int r) {
        logicOnRenderingRate = r;
    }

    public class FormPane extends JPanel {
        public FormPane() {
            entities.add(new Entity((float) (Math.random() * (W)), (float) (Math.random() * (H))));
            Timer timer = new Timer(0, e -> repaint());
            timer.start();
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(W, H);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            for (int i = 0; i < logicOnRenderingRate; i++)
                logic();
            draw(g);
        }

        private void draw(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(255, 255, 255, 255));
            g2.fillRect(0, 0, W, H);
            for (Entity a : entities) {
                g2.setColor(new Color(Math.round(a.aggressiveness * 255), Math.round(a.toxicity * 255), Math.round(a.force * 255), Math.round(a.currentHealth * 255)));
                g2.fillOval((int) (a.x - radiusEntity * Math.sqrt(a.fullness)), (int) (a.y - radiusEntity * Math.sqrt(a.fullness)), (int) (radiusEntity * Math.sqrt(a.fullness) * 2), (int) (radiusEntity * Math.sqrt(a.fullness)) * 2);
                g2.setColor(new Color(0, 0, 0, 255));
                g2.drawString("Ag " + a.aggressiveness, (int) (a.x - radiusEntity * Math.sqrt(a.fullness)), (int) (a.y - radiusEntity * Math.sqrt(a.fullness)) - 50);
                g2.drawString("Fo " + a.force, (int) (a.x - radiusEntity * Math.sqrt(a.fullness)), (int) (a.y - radiusEntity * Math.sqrt(a.fullness)) - 40);
                g2.drawString("To " + a.toxicity, (int) (a.x - radiusEntity * Math.sqrt(a.fullness)), (int) (a.y - radiusEntity * Math.sqrt(a.fullness)) - 30);
                g2.drawString("CH " + a.currentHealth, (int) (a.x - radiusEntity * Math.sqrt(a.fullness)), (int) (a.y - radiusEntity * Math.sqrt(a.fullness)) - 20);
                g2.drawString("MH " + a.maxHealth, (int) (a.x - radiusEntity * Math.sqrt(a.fullness)), (int) (a.y - radiusEntity * Math.sqrt(a.fullness)) - 10);
                g2.drawString("RH " + a.recoveryHealth, (int) (a.x - radiusEntity * Math.sqrt(a.fullness)), (int) (a.y - radiusEntity * Math.sqrt(a.fullness)));
                g2.drawString("CS " + a.currentSpeed, (int) (a.x - radiusEntity * Math.sqrt(a.fullness)), (int) (a.y - radiusEntity * Math.sqrt(a.fullness)) + 10);
                g2.drawString("MS " + a.maxSpeed, (int) (a.x - radiusEntity * Math.sqrt(a.fullness)), (int) (a.y - radiusEntity * Math.sqrt(a.fullness)) + 20);
                g2.drawString("RS " + a.recoverySpeed, (int) (a.x - radiusEntity * Math.sqrt(a.fullness)), (int) (a.y - radiusEntity * Math.sqrt(a.fullness)) + 30);
                g2.drawString("MA " + a.maxAge, (int) (a.x - radiusEntity * Math.sqrt(a.fullness)), (int) (a.y - radiusEntity * Math.sqrt(a.fullness)) + 40);
                g2.drawString("CE " + a.currentEstrusDuration, (int) (a.x - radiusEntity * Math.sqrt(a.fullness)), (int) (a.y - radiusEntity * Math.sqrt(a.fullness)) + 50);
                g2.drawString("CP " + a.currentPregnancyDuration, (int) (a.x - radiusEntity * Math.sqrt(a.fullness)), (int) (a.y - radiusEntity * Math.sqrt(a.fullness)) + 60);
                g2.drawString("LL " + (100f - Math.round(a.currentAge / (normalLifeSpan / (1f - a.maxAge)) * 100f)), (int) (a.x - radiusEntity * Math.sqrt(a.fullness)), (int) (a.y - radiusEntity * Math.sqrt(a.fullness)) + 70);
            }
            g2.setFont(new Font("default", Font.BOLD, 16));
            g2.setColor(new Color(0, 0, 0, 255));
            g2.drawString("Count cycles: " + Float.toString(countCycles), 50, 50);
            g2.drawString("Count entities: " + Float.toString(entities.size()), 50, 75);
            g2.setColor(new Color(255, 0, 0, 255));
            int countAlive = entities.size() - countDead;
            g2.drawString("Average aggressiveness: " + sumAggressiveness / countAlive, 50, 100);
            g2.setColor(new Color(0, 255, 0, 255));
            g2.drawString("Average toxicity: " + sumToxicity / countAlive, 50, 125);
            g2.setColor(new Color(0, 0, 255, 255));
            g2.drawString("Average force: " + sumForce / countAlive, 50, 150);
            g2.setColor(new Color(0, 0, 0, 255));
            g2.drawString("Average max speed: " + sumMaxSpeed / countAlive, 50, 175);
            g2.drawString("Average recovery speed: " + sumRecoverySpeed / countAlive, 50, 200);
            g2.drawString("Average max health: " + sumMaxHealth / countAlive, 50, 225);
            g2.drawString("Average recovery health: " + sumRecoveryHealth / countAlive, 50, 250);
            g2.drawString("Average max age: " + sumMaxAge / countAlive, 50, 275);
            g2.drawString("Death from exhaustion: " + deathFromExhaustion, 50, 300);
            g2.drawString("Death by killing: " + deathByKilling, 50, 325);
            g2.drawString("Death from age: " + deathFromAge, 50, 350);
            g2.drawString("Asexual reproduction: " + asexualReproduction, 50, 375);
            g2.drawString("Sexual reproduction: " + sexualReproduction, 50, 400);
            g2.setFont(new Font("default", Font.PLAIN, 10));
        }

        private void logic() {
            countCycles++;
            sumAggressiveness = 0;
            sumForce = 0;
            sumMaxSpeed = 0;
            sumRecoverySpeed = 0;
            sumToxicity = 0;
            sumMaxHealth = 0;
            sumRecoveryHealth = 0;
            sumMaxAge = 0;
            countDead = 0;
            for (int currentEntityIndex = 0; currentEntityIndex < entities.size(); currentEntityIndex++) {
                Entity currentEntity = entities.get(currentEntityIndex);
                if (currentEntity.alive) {
                    startChanges(currentEntity);
                    Entity closestEnemy1 = null;
                    Entity closestEnemy2 = null;
                    float minEnemyDist1 = maxDist;
                    float minEnemyDist2 = maxDist;
                    if (!currentEntity.estrus)
                        for (Entity e : entities) {
                            if (currentEntity != e
                                    & checkDist(currentEntity, e, sightDistance)
                                    & currentEntity.aggressiveness < e.aggressiveness
                                    & !e.pregnancy
                                    & !e.estrus) {
                                float dist = getDist(currentEntity, e);
                                if (dist < minEnemyDist1) {
                                    minEnemyDist2 = minEnemyDist1;
                                    minEnemyDist1 = dist;
                                    closestEnemy2 = closestEnemy1;
                                    closestEnemy1 = e;
                                }
                            }
                        }
                    minEnemyDist2 = (float) Math.sqrt(minEnemyDist2);
                    minEnemyDist1 = (float) Math.sqrt(minEnemyDist1);
                    Entity closestFemalePartner = null;
                    float minFemalePartnerDist = maxDist;
                    if (closestEnemy1 == null
                            & currentEntity.fullness >= 1f
                            & !currentEntity.estrus
                            & !currentEntity.pregnancy
                            & currentEntity.currentHealth == currentEntity.maxHealth)
                        for (Entity e : entities) {
                            if (currentEntity != e
                                    & checkDist(currentEntity, e, sightDistance)
                                    & e.estrus
                                    & !twin(currentEntity, e)) {
                                float dist = getDist(currentEntity, e);
                                if (dist < minFemalePartnerDist) {
                                    minFemalePartnerDist = dist;
                                    closestFemalePartner = e;
                                }
                            }
                        }
                    Entity closestFood = null;
                    int indexClosestFood = 0;
                    float minFoodDist = maxDist;
                    if (closestEnemy1 == null
                            & closestFemalePartner == null
                            & !currentEntity.estrus
                            & !currentEntity.pregnancy
                            & currentEntity.currentHealth == currentEntity.maxHealth)
                        for (Entity e : entities) {
                            if (currentEntity != e
                                    & checkDist(currentEntity, e, sightDistance)
                                    & currentEntity.aggressiveness > e.aggressiveness
                                    & !e.pregnancy
                                    & !e.estrus) {
                                float dist = getDist(currentEntity, e);
                                if (dist < minFoodDist) {
                                    minFoodDist = dist;
                                    closestFood = e;
                                    indexClosestFood = entities.indexOf(e);
                                }
                            }
                        }
                    minFoodDist = (float) Math.sqrt(minFoodDist);
                    Entity closestMalePartner = null;
                    float minMalePartnerDist = maxDistPartner;
                    if (currentEntity.currentEstrusDuration == estrusDuration)
                        for (Entity e : entities) {
                            if (currentEntity != e
                                    & e.alive
                                    & !twin(currentEntity, e)
                                    & checkDist(currentEntity, e, radiusEntity)) {
                                float dist = getDist(currentEntity, e);
                                if (dist < minMalePartnerDist) {
                                    minMalePartnerDist = dist;
                                    closestMalePartner = e;
                                }
                            }
                        }
                    currentEntityIndex = actions(currentEntityIndex, currentEntity, closestEnemy1, closestEnemy2, minEnemyDist1, minEnemyDist2, closestFemalePartner, closestFood, indexClosestFood, minFoodDist);
                    if (currentEntity.fullness >= maxFullness & currentEntity.currentHealth == currentEntity.maxHealth & !currentEntity.pregnancy & !currentEntity.estrus)
                        currentEntity.estrus = true;
                    if (currentEntity.estrus) {
                        currentEntity.currentEstrusDuration++;
                    }
                    if (currentEntity.currentEstrusDuration > estrusDuration) {
                        currentEntity.pregnancy = true;
                        currentEntity.estrus = false;
                        currentEntity.currentEstrusDuration = 0;
                        currentEntity.malePartner = closestMalePartner;
                    }
                    if (currentEntity.pregnancy) {
                        currentEntity.currentPregnancyDuration++;
                    }
                    if (currentEntity.currentPregnancyDuration > pregnancyDuration) {
                        reproduction(currentEntity);
                        currentEntity.pregnancy = false;
                        currentEntity.currentPregnancyDuration = 0;
                        currentEntity.malePartner = null;
                    }

                    pay(currentEntity);
                    sumAggressiveness += currentEntity.aggressiveness;
                    sumForce += currentEntity.force;
                    sumMaxSpeed += currentEntity.maxSpeed;
                    sumRecoverySpeed += currentEntity.recoverySpeed;
                    sumToxicity += currentEntity.toxicity;
                    sumMaxHealth += currentEntity.maxHealth;
                    sumRecoveryHealth += currentEntity.recoveryHealth;
                    sumMaxAge += currentEntity.maxAge;
                } else
                    countDead++;
            }
        }

        private void pay(Entity currentEntity) {
            currentEntity.fullness -= ((1 / (1 - currentEntity.maxSpeed)) - 1) * coefFullnessChange;
            currentEntity.fullness -= ((1 / (1 - currentEntity.toxicity)) - 1) * coefFullnessChange;
            currentEntity.fullness -= ((1 / (1 - currentEntity.aggressiveness)) - 1) * coefFullnessChange;
            currentEntity.fullness -= ((1 / (1 - currentEntity.force)) - 1) * coefFullnessChange;
            currentEntity.fullness -= ((1 / (1 - currentEntity.recoverySpeed)) - 1) * coefFullnessChange;
            currentEntity.fullness -= ((1 / (1 - currentEntity.maxHealth)) - 1) * coefFullnessChange;
            currentEntity.fullness -= ((1 / (1 - currentEntity.recoveryHealth)) - 1) * coefFullnessChange;
            currentEntity.fullness -= ((1 / (1 - currentEntity.maxAge)) - 1) * coefFullnessChange;
            if (currentEntity.fullness <= 1 / 8f) {
                dead(currentEntity);
                deathFromExhaustion++;
            }
            if (currentEntity.currentAge >= normalLifeSpan / (1f - currentEntity.maxAge)) {
                dead(currentEntity);
                deathFromAge++;
            }
        }

        private int actions(int currentEntityIndex, Entity currentEntity, Entity closestEnemy1, Entity closestEnemy2, float minEnemyDist1, float minEnemyDist2, Entity closestFemalePartner, Entity closestFood, int indexClosestFood, float minFoodDist) {
            currentEntity.useMaxSpeed = true;
            if (closestEnemy1 != null) {
                if (Math.abs(closestEnemy1.x - currentEntity.x) > sightDistance)
                    currentEntity.tx = closestEnemy1.x - currentEntity.x;
                else
                    currentEntity.tx = -closestEnemy1.x + currentEntity.x;
                if (Math.abs(closestEnemy1.y - currentEntity.y) > sightDistance)
                    currentEntity.ty = closestEnemy1.y - currentEntity.y;
                else
                    currentEntity.ty = -closestEnemy1.y + currentEntity.y;
                if (closestEnemy2 != null & minEnemyDist1 < sightDistance / 2 & minEnemyDist2 < sightDistance / 2) {
                    if (Math.abs(closestEnemy2.x - currentEntity.x) > sightDistance)
                        currentEntity.tx += closestEnemy2.x - currentEntity.x;
                    else
                        currentEntity.tx += -closestEnemy2.x + currentEntity.x;
                    if (Math.abs(closestEnemy2.y - currentEntity.y) > sightDistance)
                        currentEntity.ty += closestEnemy2.y - currentEntity.y;
                    else
                        currentEntity.ty += -closestEnemy2.y + currentEntity.y;
                }
            } else {
                if (closestFemalePartner != null) {
                    if (Math.abs(closestFemalePartner.x - currentEntity.x) > sightDistance)
                        currentEntity.tx = -closestFemalePartner.x + currentEntity.x;
                    else
                        currentEntity.tx = closestFemalePartner.x - currentEntity.x;
                    if (Math.abs(closestFemalePartner.y - currentEntity.y) > sightDistance)
                        currentEntity.ty = -closestFemalePartner.y + currentEntity.y;
                    else
                        currentEntity.ty = closestFemalePartner.y - currentEntity.y;
                } else {
                    if (closestFood != null) {
                        if (Math.abs(closestFood.x - currentEntity.x) > sightDistance)
                            currentEntity.tx = -closestFood.x + currentEntity.x;
                        else
                            currentEntity.tx = closestFood.x - currentEntity.x;
                        if (Math.abs(closestFood.y - currentEntity.y) > sightDistance)
                            currentEntity.ty = -closestFood.y + currentEntity.y;
                        else
                            currentEntity.ty = closestFood.y - currentEntity.y;
                        if (minFoodDist < radiusEntity) {
                            while (currentEntity.currentHealth > 0 & closestFood.currentHealth > 0) {
                                currentEntity.currentHealth -= closestFood.force * closestFood.aggressiveness * closestFood.force * closestFood.aggressiveness;
                                closestFood.currentHealth -= currentEntity.force * currentEntity.aggressiveness * currentEntity.force * currentEntity.aggressiveness;
                            }
                            if (currentEntity.currentHealth > 0) {
                                currentEntity.currentHealth += closestFood.force * closestFood.aggressiveness * closestFood.force * closestFood.aggressiveness;
                                currentEntity.fullness += closestFood.fullness * (0.75f - closestFood.toxicity);
                                if (closestFood.alive)
                                    deathByKilling++;
                                entities.remove(indexClosestFood);
                                if (indexClosestFood < currentEntityIndex)
                                    currentEntityIndex--;
                            } else {
                                currentEntity.currentHealth += closestFood.force * closestFood.aggressiveness * closestFood.force * closestFood.aggressiveness;
                                closestFood.currentHealth += currentEntity.force * currentEntity.aggressiveness * currentEntity.force * currentEntity.aggressiveness;
                            }

                        }
                    } else {
                        currentEntity.useMaxSpeed = false;
                        if (Math.random() < directionChangeRate) {
                            double randomAngle = Math.random() * Math.PI * 2;
                            currentEntity.tx = (float) Math.cos(randomAngle);
                            currentEntity.ty = (float) Math.sin(randomAngle);
                        }
                    }
                }
            }
            if (currentEntity.ty > sightDistance)
                currentEntity.ty = H - currentEntity.ty;
            if (currentEntity.ty < -sightDistance)
                currentEntity.ty = -H - currentEntity.ty;
            if (currentEntity.tx > sightDistance)
                currentEntity.tx = W - currentEntity.tx;
            if (currentEntity.tx < -sightDistance)
                currentEntity.tx = -W - currentEntity.tx;
            return currentEntityIndex;
        }

        private void startChanges(Entity currentEntity) {
            currentEntity.currentAge++;
            currentEntity.fullness += coefFullnessIncrease;
            if (currentEntity.currentHealth < currentEntity.maxHealth)
                currentEntity.currentHealth += currentEntity.coefHealthIncrease;
            else if (currentEntity.currentHealth > currentEntity.maxHealth)
                currentEntity.currentHealth = currentEntity.maxHealth;
            double targetAngle = Math.atan2(currentEntity.ty, currentEntity.tx);
            if (currentEntity.useMaxSpeed) {
                currentEntity.x += (float) Math.cos(targetAngle) * currentEntity.currentSpeed;
                currentEntity.y += (float) Math.sin(targetAngle) * currentEntity.currentSpeed;
                if (currentEntity.currentSpeed > currentEntity.maxSpeed / 4f)
                    currentEntity.currentSpeed *= currentEntity.coefSpeedDecrease;
            } else {
                currentEntity.x += (float) Math.cos(targetAngle) * currentEntity.currentSpeed / 4f;
                currentEntity.y += (float) Math.sin(targetAngle) * currentEntity.currentSpeed / 4f;
                currentEntity.currentSpeed += currentEntity.coefSpeedIncrease;
                if (currentEntity.currentSpeed > currentEntity.maxSpeed)
                    currentEntity.currentSpeed = currentEntity.maxSpeed;
            }
            if (currentEntity.x < 0) currentEntity.x = W;
            else if (currentEntity.x > W) currentEntity.x = 0;
            if (currentEntity.y < 0) currentEntity.y = H;
            else if (currentEntity.y > H) currentEntity.y = 0;
        }

        private void reproduction(Entity currentEntity) {
            currentEntity.fullness = currentEntity.fullness - 2f;
            Entity newEntity = new Entity(currentEntity.x + (float) Math.random() * 10 - 5, currentEntity.y + (float) Math.random() * 10 - 5);
            if (currentEntity.malePartner == null) {
                newEntity.maxSpeed = currentEntity.maxSpeed;
                newEntity.aggressiveness = currentEntity.aggressiveness;
                newEntity.force = currentEntity.force;
                newEntity.toxicity = currentEntity.toxicity;
                newEntity.recoverySpeed = currentEntity.recoverySpeed;
                newEntity.maxHealth = currentEntity.maxHealth;
                newEntity.recoveryHealth = currentEntity.recoveryHealth;
                newEntity.maxAge = currentEntity.maxAge;
                asexualReproduction++;
            } else {
                newEntity.maxSpeed = 1 - 1 / ((((1 / (1 - currentEntity.maxSpeed)) - 1) + ((1 / (1 - currentEntity.malePartner.maxSpeed)) - 1)) / 2 + 1);
                newEntity.force = 1 - 1 / ((((1 / (1 - currentEntity.force)) - 1) + ((1 / (1 - currentEntity.malePartner.force)) - 1)) / 2 + 1);
                newEntity.aggressiveness = 1 - 1 / ((((1 / (1 - currentEntity.aggressiveness)) - 1) + ((1 / (1 - currentEntity.malePartner.aggressiveness)) - 1)) / 2 + 1);
                newEntity.toxicity = 1 - 1 / ((((1 / (1 - currentEntity.toxicity)) - 1) + ((1 / (1 - currentEntity.malePartner.toxicity)) - 1)) / 2 + 1);
                newEntity.recoverySpeed = 1 - 1 / ((((1 / (1 - currentEntity.recoverySpeed)) - 1) + ((1 / (1 - currentEntity.malePartner.recoverySpeed)) - 1)) / 2 + 1);
                newEntity.maxHealth = 1 - 1 / ((((1 / (1 - currentEntity.maxHealth)) - 1) + ((1 / (1 - currentEntity.malePartner.maxHealth)) - 1)) / 2 + 1);
                newEntity.recoveryHealth = 1 - 1 / ((((1 / (1 - currentEntity.recoveryHealth)) - 1) + ((1 / (1 - currentEntity.malePartner.recoveryHealth)) - 1)) / 2 + 1);
                newEntity.maxAge = 1 - 1 / ((((1 / (1 - currentEntity.maxAge)) - 1) + ((1 / (1 - currentEntity.malePartner.maxAge)) - 1)) / 2 + 1);
                sexualReproduction++;
            }
            if (Math.random() < chanceMutation)
                switch ((int) Math.ceil(Math.random() * 8)) {
                    case 1:
                        if (Math.random() < 0.5f)
                            newEntity.maxSpeed = (float) (newEntity.maxSpeed * (1 - Math.random() * coefMutation));
                        else
                            newEntity.maxSpeed = (float) (newEntity.maxSpeed + (1 - newEntity.maxSpeed) * Math.random() * coefMutation);
                        break;
                    case 2:
                        if (Math.random() < 0.5f)
                            newEntity.aggressiveness = (float) (newEntity.aggressiveness * (1 - Math.random() * coefMutation));
                        else
                            newEntity.aggressiveness = (float) (newEntity.aggressiveness + (1 - newEntity.aggressiveness) * Math.random() * coefMutation);
                        break;
                    case 3:
                        if (Math.random() < 0.5f)
                            newEntity.toxicity = (float) (newEntity.toxicity * (1 - Math.random() * coefMutation));
                        else
                            newEntity.toxicity = (float) (newEntity.toxicity + (1 - newEntity.toxicity) * Math.random() * coefMutation);
                        break;
                    case 4:
                        if (Math.random() < 0.5f)
                            newEntity.recoverySpeed = (float) (newEntity.recoverySpeed * (1 - Math.random() * coefMutation));
                        else
                            newEntity.recoverySpeed = (float) (newEntity.recoverySpeed + (1 - newEntity.recoverySpeed) * Math.random() * coefMutation);
                        break;
                    case 5:
                        if (Math.random() < 0.5f)
                            newEntity.maxHealth = (float) (newEntity.maxHealth * (1 - Math.random() * coefMutation));
                        else
                            newEntity.maxHealth = (float) (newEntity.maxHealth + (1 - newEntity.maxHealth) * Math.random() * coefMutation);
                        break;
                    case 6:
                        if (Math.random() < 0.5f)
                            newEntity.recoveryHealth = (float) (newEntity.recoveryHealth * (1 - Math.random() * coefMutation));
                        else
                            newEntity.recoveryHealth = (float) (newEntity.recoveryHealth + (1 - newEntity.recoveryHealth) * Math.random() * coefMutation);
                        break;
                    case 7:
                        if (Math.random() < 0.5f)
                            newEntity.maxAge = (float) (newEntity.maxAge * (1 - Math.random() * coefMutation));
                        else
                            newEntity.maxAge = (float) (newEntity.maxAge + (1 - newEntity.maxAge) * Math.random() * coefMutation);
                        break;
                    case 8:
                        if (Math.random() < 0.5f)
                            newEntity.force = (float) (newEntity.force * (1 - Math.random() * coefMutation));
                        else
                            newEntity.force = (float) (newEntity.force + (1 - newEntity.force) * Math.random() * coefMutation);
                        break;
                }
            newEntity.currentSpeed = newEntity.maxSpeed;
            newEntity.currentHealth = newEntity.maxHealth;
            entities.add(newEntity);
        }

        private boolean twin(Entity currentEntity, Entity e) {
            if (Math.max(((1 / (1 - currentEntity.maxHealth)) - 1), ((1 / (1 - e.maxHealth)) - 1)) / Math.min(((1 / (1 - currentEntity.maxHealth)) - 1), ((1 / (1 - e.maxHealth)) - 1)) > 1.06f)
                return false;
            if (Math.max(((1 / (1 - currentEntity.recoveryHealth)) - 1), ((1 / (1 - e.recoveryHealth)) - 1)) / Math.min(((1 / (1 - currentEntity.recoveryHealth)) - 1), ((1 / (1 - e.recoveryHealth)) - 1)) > 1.06f)
                return false;
            if (Math.max(((1 / (1 - currentEntity.aggressiveness)) - 1), ((1 / (1 - e.aggressiveness)) - 1)) / Math.min(((1 / (1 - currentEntity.aggressiveness)) - 1), ((1 / (1 - e.aggressiveness)) - 1)) > 1.06f)
                return false;
            if (Math.max(((1 / (1 - currentEntity.force)) - 1), ((1 / (1 - e.force)) - 1)) / Math.min(((1 / (1 - currentEntity.force)) - 1), ((1 / (1 - e.force)) - 1)) > 1.06f)
                return false;
            if (Math.max(((1 / (1 - currentEntity.maxSpeed)) - 1), ((1 / (1 - e.maxSpeed)) - 1)) / Math.min(((1 / (1 - currentEntity.maxSpeed)) - 1), ((1 / (1 - e.maxSpeed)) - 1)) > 1.06f)
                return false;
            if (Math.max(((1 / (1 - currentEntity.maxAge)) - 1), ((1 / (1 - e.maxAge)) - 1)) / Math.min(((1 / (1 - currentEntity.maxAge)) - 1), ((1 / (1 - e.maxAge)) - 1)) > 1.06f)
                return false;
            if (Math.max(((1 / (1 - currentEntity.recoverySpeed)) - 1), ((1 / (1 - e.recoverySpeed)) - 1)) / Math.min(((1 / (1 - currentEntity.recoverySpeed)) - 1), ((1 / (1 - e.recoverySpeed)) - 1)) > 1.06f)
                return false;
            if (Math.max(((1 / (1 - currentEntity.toxicity)) - 1), ((1 / (1 - e.toxicity)) - 1)) / Math.min(((1 / (1 - currentEntity.toxicity)) - 1), ((1 / (1 - e.toxicity)) - 1)) > 1.06f)
                return false;
            return true;
        }

        private void dead(Entity currentEntity) {
            currentEntity.aggressiveness = 0;
            currentEntity.force = 0;
            currentEntity.estrus = false;
            currentEntity.pregnancy = false;
            currentEntity.alive = false;
        }

        private float getDist(Entity currentEntity, Entity e) {
            float dist1 = (currentEntity.x - e.x) * (currentEntity.x - e.x) + (currentEntity.y - e.y) * (currentEntity.y - e.y);
            float dist2 = (W - Math.abs(currentEntity.x - e.x)) * (W - Math.abs(currentEntity.x - e.x)) + (currentEntity.y - e.y) * (currentEntity.y - e.y);
            float dist3 = (currentEntity.x - e.x) * (currentEntity.x - e.x) + (H - Math.abs(currentEntity.y - e.y)) * (H - Math.abs(currentEntity.y - e.y));
            float dist4 = (W - Math.abs(currentEntity.x - e.x)) * (W - Math.abs(currentEntity.x - e.x)) + (H - Math.abs(currentEntity.y - e.y)) * (H - Math.abs(currentEntity.y - e.y));
            return Math.min(Math.min(dist1, dist2), Math.min(dist3, dist4));
        }

        private boolean checkDist(Entity currentEntity, Entity e, float sightDistance) {
            return (Math.abs(currentEntity.x - e.x) < sightDistance) | (W - Math.abs(currentEntity.x - e.x) < sightDistance)
                    & (Math.abs(currentEntity.y - e.y) < sightDistance) | (H - Math.abs(currentEntity.y - e.y) < sightDistance);
        }
    }
}