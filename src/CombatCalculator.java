import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class CombatCalculator {
  public enum Weapon {
    NOTHING(0, 0, "Nothing"),
    RUNE_SCIMITAR(45, 44, "Rune Scimitar"),
    DRAGON_LONGSWORD(69, 71, "Dragon Longsword"),
    DRAGON_SWORD(65, 63, "Dragon Sword"),
    DRAGON_SPEAR(55, 60, "Dragon Spear"),
    GRANITE_MAUL(81, 79, "Granite Maul"),
    AMULET_OF_POWER(6, 6, "Amulet of Power"),
    AMULET_OF_GLORY(10, 6, "Amulet of Glory"),
    AMULET_OF_STRENGTH(0, 10, "Amulet of Strength");
    private final int attackBonus;
    private final int strengthBonus;
    private final String name;
    Weapon(int att, int str, String nam) {
      attackBonus = att;
      strengthBonus = str;
      name = nam;
    }
    public int getAttackBonus() {
      return attackBonus;
    }
    public int getStrengthBonus() {
      return strengthBonus;
    }
    public String getName() {
      return name;
    }
    @Override
    public String toString() {
      return getName();
    }
  }
  public enum Stance {
    ACCURATE(3, 0, "Accurate"),
    AGGRESSIVE(0, 3, "Aggressive");
    private final int attackBonus;
    private final int strengthBonus;
    private final String name;
    private Stance(int attackBonus, int strengthBonus, String name) {
      this.attackBonus = attackBonus;
      this.strengthBonus = strengthBonus;
      this.name = name;
    }
    public int getAttackBonus() {
      return attackBonus;
    }
    public int getStrengthBonus() {
      return strengthBonus;
    }
    @Override
    public String toString() {
      return name;
    }
  }
  public enum Enemy {
    HILL_GIANT(26, 0, "Hill Giant"),
    OBOR(60, 40, "Obor");
    private final int defenseLevel;
    private final int defenseBonus;
    private final String name;
    private Enemy(int defenseLevel, int defenseBonus, String name) {
      this.defenseLevel = defenseLevel;
      this.defenseBonus = defenseBonus;
      this.name = name;
    }
    public int getDefenseLevel() {
      return defenseLevel;
    }
    public int getDefenseBonus() {
      return defenseBonus;
    }
    @Override
    public String toString() {
      return name;
    }
  }
  JFrame frame;
  JPanel panel;
  JTextField attackLevelField;
  JTextField strengthLevelField;
  JComboBox<Weapon> itemBox1;
  JComboBox<Weapon> itemBox2;
  JComboBox<Stance> stanceBox;
  JComboBox<Enemy> enemyBox;
  JLabel hitChance;
  JLabel maxHit;
  
  Font font;
  Dimension normal;
  Dimension wide;
  
  ActionListener listener;
  public CombatCalculator() {
    frame = new JFrame("Combat Calculator");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setSize(800, 500);
    
    panel = new JPanel();
    frame.add(panel);
    
    normal = new Dimension(100, 50);
    wide = new Dimension(200, 50);
    font = new Font("Times", Font.PLAIN, 20);
    listener = (e) -> {
      compute();
    };
    
    attackLevelField = new JTextField("50");
    attackLevelField.getDocument().addDocumentListener(new DocumentListener() {
      @Override
      public void insertUpdate(DocumentEvent e) {
        compute();
      }
      @Override
      public void removeUpdate(DocumentEvent e) {
        compute();
      }
      @Override
      public void changedUpdate(DocumentEvent e) {
        compute();
      }
    });
    attackLevelField.addActionListener(listener);
    attackLevelField.setFont(font);
    attackLevelField.setPreferredSize(normal);
    attackLevelField.setToolTipText("Attack Level");
    panel.add(attackLevelField);
    strengthLevelField = new JTextField("60");
    strengthLevelField.getDocument().addDocumentListener(new DocumentListener() {
      @Override
      public void insertUpdate(DocumentEvent e) {
        compute();
      }
      @Override
      public void removeUpdate(DocumentEvent e) {
        compute();
      }
      @Override
      public void changedUpdate(DocumentEvent e) {
        compute();
      }
    });
    strengthLevelField.addActionListener(listener);
    strengthLevelField.setFont(font);
    strengthLevelField.setPreferredSize(normal);
    strengthLevelField.setToolTipText("Strength Level");
    panel.add(strengthLevelField);
    
    itemBox1 = new JComboBox<Weapon>(Weapon.values());
    itemBox1.addActionListener(listener);
    itemBox1.setFont(font);
    itemBox1.setPreferredSize(wide);
    panel.add(itemBox1);
    itemBox2 = new JComboBox<Weapon>(Weapon.values());
    itemBox2.addActionListener(listener);
    itemBox2.setFont(font);
    itemBox2.setPreferredSize(wide);
    panel.add(itemBox2);
    stanceBox = new JComboBox<Stance>(Stance.values());
    stanceBox.addActionListener(listener);
    stanceBox.setFont(font);
    stanceBox.setPreferredSize(wide);
    panel.add(stanceBox);
    enemyBox = new JComboBox<Enemy>(Enemy.values());
    enemyBox.addActionListener(listener);
    enemyBox.setFont(font);
    enemyBox.setPreferredSize(wide);
    panel.add(enemyBox);
    
    hitChance = new JLabel("Hit Chance");
    hitChance.setToolTipText("Chance to Hit");
    hitChance.setFont(font);
    hitChance.setPreferredSize(normal);
    panel.add(hitChance);
    maxHit = new JLabel("Max Hit");
    maxHit.setToolTipText("Maximum Damage");
    maxHit.setFont(font);
    maxHit.setPreferredSize(normal);
    panel.add(maxHit);
    
    frame.setVisible(true);
    compute();
  }
  
  public void compute() {
    int strengthLevel = 1;
    int attackLevel = 1;
    try {
      strengthLevel = Integer.parseInt(strengthLevelField.getText());
    } catch (Exception e ) {
    }
    try {
      attackLevel = Integer.parseInt(attackLevelField.getText());
    } catch (Exception e ) {
    }
    int itemAttackBonus = ((Weapon)itemBox1.getSelectedItem()).getAttackBonus() + ((Weapon)(itemBox2.getSelectedItem())).getAttackBonus();
    int itemStrengthBonus = ((Weapon)itemBox1.getSelectedItem()).getStrengthBonus() + ((Weapon)(itemBox2.getSelectedItem())).getStrengthBonus();
    int stanceAttackBonus = ((Stance)stanceBox.getSelectedItem()).getAttackBonus();
    int stanceStrengthBonus = ((Stance)stanceBox.getSelectedItem()).getStrengthBonus();
    int attackRoll = (itemAttackBonus + 64) * (8 + stanceAttackBonus + attackLevel );
    
    int enemyDefenseLevel = ((Enemy)enemyBox.getSelectedItem()).getDefenseLevel();
    int enemyDefenseBonus = ((Enemy)enemyBox.getSelectedItem()).getDefenseBonus();
    int defenseRoll = (8 + enemyDefenseLevel) * (64 + enemyDefenseBonus);
    System.err.println("Attack roll = " + attackRoll + ", def roll = " + defenseRoll);
    double missChance = (defenseRoll + 2.0) / (2.0 * attackRoll + 2.0);
//    if( defenseRoll > attackRoll ) {
//      
//    }
    double accuracy = 1.0 - missChance;
    hitChance.setText(String.format("%.2f", accuracy*100));
    int effectiveStrength = stanceStrengthBonus + strengthLevel;
    int maxHitDamage = (int) (1.3 + effectiveStrength/10.0 + itemStrengthBonus/80.0 + effectiveStrength*itemStrengthBonus/640.0);
    maxHit.setText(maxHitDamage + "");
  }

  public static void main(String[] args) {
    new CombatCalculator();
  }

}
