import jason.asSyntax.*;
import jason.environment.Environment;
import jason.environment.grid.GridWorldModel;
import jason.environment.grid.GridWorldView;
import jason.environment.grid.Location;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.util.Random;
import java.util.logging.Logger;

public class MarsEnv extends Environment {

    public static final int GSize = 7; // grid size
    public static final int GARB  = 16; // garbage code in grid model

    public static final Term    ns = Literal.parseLiteral("next(slot)");
	public static final Term    nsc = Literal.parseLiteral("nextContinous(slot)");
	public static final Term    nrdm = Literal.parseLiteral("nextRandom(slot)");
    public static final Term    pg = Literal.parseLiteral("pick(garb)");
    public static final Term    dg = Literal.parseLiteral("drop(garb)");
    public static final Term    bg = Literal.parseLiteral("burn(garb)");
	public static final Term    dgs = Literal.parseLiteral("dropGarb(slot)");
	public static final Term    dngs = Literal.parseLiteral("dropNewGarb(slot)");
    public static final Literal g1 = Literal.parseLiteral("garbage(r1)");
    public static final Literal g2 = Literal.parseLiteral("garbage(r2)");
	public static final Literal g3 = Literal.parseLiteral("garbage(r3)");

    static Logger logger = Logger.getLogger(MarsEnv.class.getName());

    private MarsModel model;
    private MarsView  view;

    @Override
    public void init(String[] args) {
        model = new MarsModel();
        view  = new MarsView(model);
        model.setView(view);
        updatePercepts();
    }

    @Override
    public boolean executeAction(String ag, Structure action) {
        logger.info(ag+" doing: "+ action);
        try {
            if (action.equals(ns)) {
                model.nextSlot();
            } else if (action.getFunctor().equals("move_towards")) {
                int x = (int)((NumberTerm)action.getTerm(0)).solve();
                int y = (int)((NumberTerm)action.getTerm(1)).solve();
                model.moveTowards(x,y);
            } else if (action.getFunctor().equals("showBattery")){
				model.showBattery((int)((NumberTerm)action.getTerm(0)).solve());
			}else if (action.equals(pg)) {
                model.pickGarb();
            } else if (action.equals(dg)) {
                model.dropGarb();
            } else if (action.equals(bg)) {
                model.burnGarb();
            } else if (action.equals(nsc)){
				model.nextSlotContinous();
			/*} else if (action.equals(ntdc)){
			    model.nextSlotTopDownContinous();*/
			} else if (action.equals(nrdm)){
				model.nextRandom();
			} else if (action.equals(dngs)) {
				model.dropNewGarb(2);
			} else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        updatePercepts();

        try {
            Thread.sleep(200);
        } catch (Exception e) {}
        informAgsEnvironmentChanged();
        return true;
    }

    /** creates the agents perception based on the MarsModel */
    void updatePercepts() {
        clearPercepts();

        Location r1Loc = model.getAgPos(0);
        Location r2Loc = model.getAgPos(1);
		Location r3Loc = model.getAgPos(2);
		Location r4Loc = model.getAgPos(3);

        Literal pos1 = Literal.parseLiteral("pos(r1," + r1Loc.x + "," + r1Loc.y + ")");
        Literal pos2 = Literal.parseLiteral("pos(r2," + r2Loc.x + "," + r2Loc.y + ")");
		Literal pos3 = Literal.parseLiteral("pos(r3," + r3Loc.x + "," + r3Loc.y + ")");
		Literal pos4 = Literal.parseLiteral("pos(r4," + r4Loc.x + "," + r4Loc.y + ")");

        addPercept(pos1);
        addPercept(pos2);
		addPercept(pos3);
		addPercept(pos4);

        if (model.hasObject(GARB, r1Loc)) {
            addPercept(g1);
        }
        if (model.hasObject(GARB, r2Loc)) {
            addPercept(g2);
        }
		if (model.hasObject(GARB, r3Loc)) {
            addPercept(g3);
        }
    }


    class MarsModel extends GridWorldModel {

        public static final int MErr = 2; // max error in pick garb
		public static final int BErr = 2; // max error in pick garb
        int nerr; // number of tries of pick garb
		int berr; // number of tries of burning garb
        boolean r1HasGarb = false; // whether r1 is carrying garbage or not
		boolean dir = true; //right direction
		boolean dirT = false; // down direction
		int r1Score = 0;
        Random random = new Random(System.currentTimeMillis());
		
		int batteryLevel = 0;
		boolean showBattery = false;
		

        private MarsModel() {
            super(GSize, GSize, 4);

            // initial location of agents
            try {
                setAgPos(0, 0, 0);

                //Location r2Loc = new Location(GSize/2, GSize/2);
				setAgPos(0,getFreePos());
                setAgPos(1, getFreePos());
				setAgPos(2, getFreePos());
				setAgPos(3,getFreePos());
            } catch (Exception e) {
                e.printStackTrace();
            }

            // initial location of garbage
			for(int i = 0; i < 10; i++){
				add(GARB,getFreePos());
			}
            /*add(GARB, 3, 0);
            add(GARB, GSize-1, 0);
            add(GARB, 1, 2);
            add(GARB, 0, GSize-2);
            add(GARB, GSize-1, GSize-1);*/
        }
		
		void nextSlot() throws Exception {
            Location r1 = getAgPos(0);
			
			if(dir){
				r1.x++;
				if (r1.x == getWidth()) {
					r1.x = 0;
                	r1.y++;
				}
			}else {
				r1.x--;
				if (r1.x == -1) {
					r1.x = getWidth()-1;
                	r1.y--;
				}
			}
            
            // finished searching the whole grid
            if ((dir && r1.y == getHeight()) || (!dir && r1.y == -1)) {
                dir = !dir;
				return;
            }
			
            setAgPos(0, r1);
            setAgPos(1, getAgPos(1)); // just to draw it in the view
			setAgPos(2, getAgPos(2)); // just to draw it in the view
			setAgPos(3, getAgPos(3)); // just to draw it in the view
        }

        void nextSlotContinous() throws Exception {
            Location r1 = getAgPos(0);
			
			if(dir){
				r1.x++;
				if (r1.x == getWidth()) {
					r1.x = 0;
                	r1.y++;
				}
			}else {
				r1.x--;
				if (r1.x == -1) {
					r1.x = getWidth()-1;
                	r1.y--;
				}
			}
            
            // finished searching the whole grid
            if ((dir && r1.y == getHeight()) || (!dir && r1.y == -1)) {
                dir = !dir;
				return;
            }
			
            setAgPos(0, r1);
            setAgPos(1, getAgPos(1)); // just to draw it in the view
			setAgPos(2, getAgPos(2)); // just to draw it in the view
			setAgPos(3, getAgPos(3)); // just to draw it in the view
        }
		
		void nextRandom() throws Exception {
			Location r3 = getAgPos(2);
			
			int rDir = random.nextInt(8);
			switch(rDir){
				case 0: //TL
					if ( r3.x != 0 && r3.y != 0 ){
						r3.x--;
						r3.y--;
					}
				break;
				case 1: // T
					if(r3.y != 0 ){
						r3.y--;
					}
				break; 
				case 2: // TR
					if(r3.x != getWidth()-1 && r3.y != 0){
						r3.x++;
						r3.y--;
					}
				break; 
				case 3: // R
					if(r3.x != getWidth()-1 ){
						r3.x++;
					}
				break;
				case 4: // DR
					if(r3.x != getWidth()-1 && r3.y != getHeight()-1){
						r3.x++;
						r3.y++;
					}
				break;
				case 5: // D
					if(r3.y != getHeight()-1 ){
						r3.y++;
					}
				break;
				case 6: // DL
					if(r3.x != 0 && r3.y != getHeight()-1){
						r3.x--;
						r3.y++;
					}
				break;
				case 7: // L
					if(r3.x != 0 ){
						r3.x--;
					}
				break;
			}
			
			
			
			setAgPos(2, r3);
            setAgPos(0, getAgPos(0)); // just to draw it in the view
			setAgPos(1, getAgPos(1)); // just to draw it in the view
			setAgPos(3, getAgPos(3)); // just to draw it in the view
			
			//dropNewGarb(2);
		}
		
		void nextSlotTopDownContinous() throws Exception { //R3
            Location r3 = getAgPos(2);
			
			if(!dirT){
				r3.y++;
				if (r3.y == getHeight()) {
					r3.y = 0;
                	r3.x++;
				}
			}else {
				r3.y--;
				if (r3.y == -1) {
					r3.y = getHeight()-1;
                	r3.x--;
				}
			}
            
            // finished searching the whole grid
            if ((dirT && r3.x == -1) || (!dirT && r3.x == getWidth())) {
                dirT = !dirT;
				return;
            }
			
			//dropNewGarb(2);
			
            setAgPos(2, r3);
            setAgPos(0, getAgPos(0)); // just to draw it in the view
			setAgPos(1, getAgPos(1)); // just to draw it in the view
			setAgPos(3, getAgPos(3)); // just to draw it in the view
        }

        void moveTowards(int x, int y) throws Exception {
            Location r1 = getAgPos(0);
            if (r1.x < x)
                r1.x++;
            else if (r1.x > x)
                r1.x--;
            if (r1.y < y)
                r1.y++;
            else if (r1.y > y)
                r1.y--;
            setAgPos(0, r1);
            setAgPos(1, getAgPos(1)); // just to draw it in the view
			setAgPos(2, getAgPos(2)); // just to draw it in the view
			setAgPos(3, getAgPos(3)); // just to draw it in the view
        }

        void pickGarb() {
            // r1 location has garbage
            if (model.hasObject(GARB, getAgPos(0))) {
                // sometimes the "picking" action doesn't work
                // but never more than MErr times
                if (random.nextBoolean() || nerr == MErr) {
                    remove(GARB, getAgPos(0));
                    nerr = 0;
                    r1HasGarb = true;
					this.r1Score +=10;
                } else {
                    nerr++;
					this.r1Score -=5;
                }
            }
        }
        void dropGarb() {
            if (r1HasGarb) {
                r1HasGarb = false;
                add(GARB, getAgPos(0));
            }
        }
        void burnGarb() {
            // r2 location has garbage
            if (model.hasObject(GARB, getAgPos(1))) {
				if (random.nextBoolean() || berr == BErr) {
                    remove(GARB, getAgPos(1));
                    berr = 0;
                } else {
                    berr++;
                }       
            }
        }
		
		void dropNewGarb(int ag) {
			if(!model.hasObject(GARB,getAgPos(ag))){
				if(random.nextDouble() < 0.1){
					add(GARB, getAgPos(2));
				}
			}
		}
		
		void showBattery(int battery){
			batteryLevel = battery;
			showBattery = true;
		}
    }
	

    class MarsView extends GridWorldView {
		MarsModel model;
        public MarsView(MarsModel model) {
            super(model, "Mars World", 600);
			this.model = model;
            defaultFont = new Font("Arial", Font.BOLD, 18); // change default font
            setVisible(true);
            repaint();
        }

        /** draw application objects */
        @Override
        public void draw(Graphics g, int x, int y, int object) {
            switch (object) {
            case MarsEnv.GARB:
                drawGarb(g, x, y);
                break;
            }
        }

        @Override
        public void drawAgent(Graphics g, int x, int y, Color c, int id) {
            String label = "R"+(id+1);
			if(id == 0) label = ""+this.model.r1Score;
            c = Color.blue;
            if (id == 0) {
                c = Color.yellow;
                if (((MarsModel)model).r1HasGarb) {
                    label += " - G";
                    c = Color.orange;
                }else if(((MarsModel)model).showBattery) {
					int batteryLevel = ((MarsModel)model).batteryLevel;
					label += "-" + Integer.toString(batteryLevel) + "%";
					if(batteryLevel < 20){
						c = Color.red;
					}else if(batteryLevel < 50){
						c = Color.orange;
					}
				}
            }
			if (id == 2) {
				c = Color.green;
			}
			if (id == 3) {
				c = Color.pink;
			}
            super.drawAgent(g, x, y, c, -1);
            if (id == 0) {
                g.setColor(Color.black);
            } else {
                g.setColor(Color.white);
            }
            super.drawString(g, x, y, defaultFont, label);
            //repaint();
        }

        public void drawGarb(Graphics g, int x, int y) {
            super.drawObstacle(g, x, y);
            g.setColor(Color.white);
            drawString(g, x, y, defaultFont, "G");
        }

    }
}
