import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class CombatCalculator {
  
  public static final int ATTACK_LEVEL = 65;
  public static final int STRENGTH_LEVEL = 75;
  
  private boolean mouseOn;
  private int mousePosition;
  
  public enum Weapon {
    RUNE_SCIMITAR(45, 44, "Rune Scimitar"),
    AMULET_OF_POWER(6, 6, "Amulet of Power"),
    AMULET_OF_STRENGTH(0, 10, "Amulet of Strength"),
    RUNE_2H(69, 70, "Rune 2H"),
    MAGIC_GEAR(25, 0, "Magic Gear"),
    DRAGON_LONGSWORD(69, 71, "Dragon Longsword"),
    DRAGON_SWORD(65, 63, "Dragon Sword"),
    DRAGON_SPEAR(55, 60, "Dragon Spear"),
    GRANITE_MAUL(81, 79, "Granite Maul"),
    AMULET_OF_GLORY(10, 6, "Amulet of Glory"),
    NOTHING(0, 0, "Nothing");
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
    MOSS_GIANT(30, 0, "Moss Giant"),
    MINOTAUR_27(25, -21, "27 Minotaur"),
    GUARD(14, -4, "Guard"),
    LESSER_DEMON(71, 0, "Lesser Demon"),
    SKELETON_159(130, 0, "159 Skeleton"),
    BLUE_DRAGON(95, 70, "Blue Dragon"),
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
  
  JPanel attackGraph;
  
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
    
    attackLevelField = new JTextField(ATTACK_LEVEL + "");
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
    strengthLevelField = new JTextField(STRENGTH_LEVEL + "");
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
//    maxHit.setFont(font);
    maxHit.setPreferredSize(new Dimension(700, 100));
    panel.add(maxHit);
    attackGraph = new JPanel() {
      @Override
      public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, getWidth()-1, getHeight()-1);
        int[] graphHeight = new int[99];
        for( int attackLevel = 1; attackLevel <= 99; attackLevel++ ) {
          double accuracy = getHitChance(attackLevel);
          graphHeight[attackLevel-1] = (int) ((1-accuracy) * getHeight());
        }
        for( int chance = 1; chance <= 9; chance++ ) {
          g.setColor(Color.LIGHT_GRAY);
          int pixelHeight = (int) ((1-chance*0.1) * getHeight());
          g.drawLine(0, pixelHeight, getWidth()-1, pixelHeight);
          g.setColor(Color.BLACK);
          g.drawString(chance*10 + "%", 1, pixelHeight + 5);
        }
        int width = getWidth()/99;
        for( int level = 10; level <= 90; level+=10 ) {
          g.setColor(Color.LIGHT_GRAY);
          g.drawLine(level * width, 0, level * width, getHeight()-1);
          g.setColor(Color.BLACK);
          g.drawString(level + "", level * width - 7, getHeight() - 2);
        }
        g.setColor(Color.black);
        g.drawRect(0, 0, getWidth()-1, getHeight()-1);
        g.drawString("Accuracy vs Attack Level", 25, 12);
        g.setColor(Color.black);
//        g.drawLine(0, graphHeight[0], width, graphHeight[0]);
        for( int i = 1; i < graphHeight.length; i++ ) {
          g.drawLine((i)*width, graphHeight[i-1], (i+1)*width, graphHeight[i]);
        }
        g.setColor(Color.gray);
        if( mouseOn ) {
          int selected = mousePosition / width;
          double acc = getHitChance(selected);
          g.drawLine(selected*width, 0, selected*width, getHeight()-1);
          g.setColor(Color.black);
          g.drawString(String.format("%.1f", acc * 100), selected*width-27, getHeight() - 22);
        }
        else {
          g.drawLine(getAttackLevel()*width, 0, getAttackLevel()*width, getHeight()-1);
          g.setColor(Color.black);
          g.drawString(String.format("%.1f", getHitChance(getAttackLevel()) * 100), getAttackLevel()*width-27, getHeight() - 22);
        }
      }
    };
    attackGraph.addMouseMotionListener(new MouseMotionListener() {
      @Override
      public void mouseDragged(MouseEvent e) {
        mousePosition = e.getX();
        frame.repaint();
      }
      @Override
      public void mouseMoved(MouseEvent e) {
        mousePosition = e.getX();
        frame.repaint();
      }
    });
    attackGraph.addMouseListener(new MouseListener() {
      @Override
      public void mouseClicked(MouseEvent arg0) {
        
      }
      @Override
      public void mouseEntered(MouseEvent arg0) {
        mouseOn = true;
        frame.repaint();
      }
      @Override
      public void mouseExited(MouseEvent arg0) {
        mouseOn = false;
        frame.repaint();
      }
      @Override
      public void mousePressed(MouseEvent arg0) {
        int selected = mousePosition / (attackGraph.getWidth()/99);
        attackLevelField.setText(selected + "");
      }
      @Override
      public void mouseReleased(MouseEvent arg0) {
        
      }
    });
    attackGraph.setPreferredSize(new Dimension(400, 200));
    panel.add(attackGraph);
    
    frame.setAlwaysOnTop(true);
    frame.setVisible(true);
    compute();
    itemBox2.setSelectedIndex(2);
  }
  public double getHitChance(int attackLevel) {
    int itemAttackBonus = ((Weapon)itemBox1.getSelectedItem()).getAttackBonus() + ((Weapon)(itemBox2.getSelectedItem())).getAttackBonus();
    int stanceAttackBonus = ((Stance)stanceBox.getSelectedItem()).getAttackBonus();
    int attackRoll = (itemAttackBonus + 64) * (8 + stanceAttackBonus + attackLevel );
    int enemyDefenseLevel = ((Enemy)enemyBox.getSelectedItem()).getDefenseLevel();
    int enemyDefenseBonus = ((Enemy)enemyBox.getSelectedItem()).getDefenseBonus();
    int defenseRoll = (8 + enemyDefenseLevel) * (64 + enemyDefenseBonus);
//    System.err.println("Attack roll = " + attackRoll + ", def roll = " + defenseRoll);
    double missChance = (defenseRoll + 2.0) / (2.0 * attackRoll + 2.0);
    double accuracy = 1.0 - missChance;
    if( defenseRoll > attackRoll ) {
      accuracy = 1.0*attackRoll / ( 2 * ( defenseRoll + 1) ); 
    }
    return accuracy;
  }
  public int getAttackLevel() {
    int attackLevel = 1;
    try {
      attackLevel = Integer.parseInt(attackLevelField.getText());
    } catch (Exception e ) {
    }
    return attackLevel;
  }
  public void compute() {
    int strengthLevel = 1;
    try {
      strengthLevel = Integer.parseInt(strengthLevelField.getText());
    } catch (Exception e ) {
    }
    hitChance.setText(String.format("%.2f", getHitChance(getAttackLevel())*100));

    String maxString = "";
    int itemStrengthBonus = ((Weapon)itemBox1.getSelectedItem()).getStrengthBonus() + ((Weapon)(itemBox2.getSelectedItem())).getStrengthBonus();
    int stanceStrengthBonus = ((Stance)stanceBox.getSelectedItem()).getStrengthBonus();
    for( int str = strengthLevel; str <= strengthLevel + (3 + (int)(strengthLevel/10)); str++ ) {
      int effectiveStrength = stanceStrengthBonus + str;
      double maxHitDamage =  (1.3 + effectiveStrength/10.0 + itemStrengthBonus/80.0 + effectiveStrength*itemStrengthBonus/640.0);
      maxString += str + "=" + String.format("%.2f", maxHitDamage);
      if( str != strengthLevel-9 ) {
        maxString += ", ";
      }
    }
    maxHit.setText(maxString);
    frame.repaint();
  }

  public static void main(String[] args) {
//    new StatsDriver();
    new CombatCalculator();
  }

}
