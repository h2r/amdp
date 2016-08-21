package amdp.taxi;



import amdp.taxi.state.*;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.visualizer.OOStatePainter;
import burlap.visualizer.ObjectPainter;
import burlap.visualizer.StateRenderLayer;
import burlap.visualizer.Visualizer;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @author ngopalan.
 */
public class TaxiVisualizer {

    Map<String, Color> colourMap = new HashMap<String, Color>();

    static String imagePathNakul = "amdp/data/resources/taxiImages/";


    private TaxiVisualizer(){
        initializeColours();
//
//        darkerColourMap.put(TaxiDomain.DARKGREY, Color.darkGray.darker());
//        darkerColourMap.put(TaxiDomain.RED, Color.red.darker());
//        darkerColourMap.put(TaxiDomain.GREEN, Color.green.darker());
//        darkerColourMap.put(TaxiDomain.BLUE, Color.blue);
//        darkerColourMap.put(TaxiDomain.YELLOW, Color.yellow);
//        darkerColourMap.put(TaxiDomain.MAGENTA, Color.magenta);
//        darkerColourMap.put(TaxiDomain.PINK, Color.pink);
//        darkerColourMap.put(TaxiDomain.ORANGE, Color.orange);
//        darkerColourMap.put(TaxiDomain.CYAN, Color.cyan);

    };
    @Deprecated
    public static Visualizer getVisualizer(int w, int h){
        Visualizer v = new Visualizer(getStateRenderLayer(w, h));
        return v;
    }


    private void initializeColours(){
        colourMap.put(TaxiDomain.DARKGREY, Color.darkGray);
        colourMap.put(TaxiDomain.RED, Color.red);
        colourMap.put(TaxiDomain.GREEN, Color.green);
        colourMap.put(TaxiDomain.BLUE, Color.blue);
        colourMap.put(TaxiDomain.YELLOW, Color.yellow);
        colourMap.put(TaxiDomain.MAGENTA, Color.magenta);
        colourMap.put(TaxiDomain.PINK, Color.pink);
        colourMap.put(TaxiDomain.ORANGE, Color.orange);
        colourMap.put(TaxiDomain.CYAN, Color.cyan);
    }

    public static StateRenderLayer getStateRenderLayer(int w, int h){

        StateRenderLayer rl = new StateRenderLayer();
        OOStatePainter oopainter = new OOStatePainter();

        oopainter.addObjectClassPainter(TaxiDomain.LOCATIONCLASS, new LocationPainter(w, h, 0, true));
        oopainter.addObjectClassPainter(TaxiDomain.TAXICLASS, new CellPainter(1, Color.gray, w, h));
        oopainter.addObjectClassPainter(TaxiDomain.PASSENGERCLASS, new PassengerPainter(w, h, 1, false));
        oopainter.addObjectClassPainter(TaxiDomain.WALLCLASS, new WallPainter(w, h));

        rl.addStatePainter(oopainter);

        return rl;

    }



    /**
     * A painter for a grid world cell which will fill the cell with a given color and where the cell position
     * is indicated by the x and y attribute for the mapped object instance
     * @author James MacGlashan
     *
     */
    public static class CellPainter implements ObjectPainter {

        protected Color			col;
        protected int			dwidth;
        protected int			dheight;
        protected int			shape = 0; //0 for rectangle 1 for ellipse




        /**
         * Initializes painter for a rectangle shape cell
         * @param col the color to paint the cell
         */
        public CellPainter(Color col, int w, int h) {
            this.col = col;
            this.dwidth = w;
            this.dheight = h;
        }

        /**
         * Initializes painter with filling the cell with the given shape
         * @param shape the shape with which to fill the cell. 0 for a rectangle, 1 for an ellipse.
         * @param col the color to paint the cell
         */
        public CellPainter(int shape, Color col, int w, int h) {
            this.col = col;
            this.dwidth = w;
            this.dheight = h;
            this.shape = shape;
        }



        @Override
        public void paintObject(Graphics2D g2, OOState s, ObjectInstance ob, float cWidth, float cHeight) {


            //set the color of the object
            g2.setColor(this.col);

            float domainXScale = this.dwidth;
            float domainYScale = this.dheight;

            //determine then normalized width
            float width = (1.0f / domainXScale) * cWidth;
            float height = (1.0f / domainYScale) * cHeight;

            float rx = ((TaxiAgent)ob).x*width;
            float ry = cHeight - height - ((TaxiAgent)ob).y*height;

            if(this.shape == 0){
                g2.fill(new Rectangle2D.Float(rx, ry, width, height));
            }
            else{
                g2.fill(new Ellipse2D.Float(rx, ry, width, height));
            }

        }




    }



    /**
     * A painter for location objects which will fill the cell with a given color and where the cell position
     * is indicated by the x and y attribute for the mapped object instance
     * @author James MacGlashan
     *
     */
    public static class LocationPainter implements ObjectPainter{

//        protected java.util.List<Color> baseColors;
        protected int			dwidth;
        protected int			dheight;
        protected int			shape = 0; //0 for rectangle 1 for ellipse
        protected boolean       darken;

        protected Map<String, Color> colourMap = new HashMap<String, Color>();


        /**
         * Initializes painter
         */
        public LocationPainter(int w, int h, int shape, boolean darken) {
            this.dwidth = w;
            this.dheight = h;
            this.shape = shape;
            this.darken = darken;
            this.initializeColours();

//
//            if(darken){
//                java.util.List<Color> dcols = new ArrayList<Color>(9);
//                for(Color c : this.baseColors){
//                    dcols.add(c.darker());
//                }
//                this.baseColors = dcols;
//            }

        }

        private void initializeColours(){
            colourMap.put(TaxiDomain.DARKGREY, Color.darkGray);
            colourMap.put(TaxiDomain.RED, Color.red);
            colourMap.put(TaxiDomain.GREEN, Color.green);
            colourMap.put(TaxiDomain.BLUE, Color.blue);
            colourMap.put(TaxiDomain.YELLOW, Color.yellow);
            colourMap.put(TaxiDomain.MAGENTA, Color.magenta);
            colourMap.put(TaxiDomain.PINK, Color.pink);
            colourMap.put(TaxiDomain.ORANGE, Color.orange);
            colourMap.put(TaxiDomain.CYAN, Color.cyan);
        }

        @Override
        public void paintObject(Graphics2D g2, OOState s, ObjectInstance ob, float cWidth, float cHeight) {


            String colour = ((TaxiLocation)ob).colour;

            Color col = darken? colourMap.get(colour).darker() : colourMap.get(colour);


            //set the color of the object
            g2.setColor(col);

            float domainXScale = this.dwidth;
            float domainYScale = this.dheight;

            //determine then normalized width
            float width = (1.0f / domainXScale) * cWidth;
            float height = (1.0f / domainYScale) * cHeight;

            float rx = ((TaxiLocation)ob).x*width;
            float ry = cHeight - height - ((TaxiLocation)ob).y*height;

            if(this.shape == 0){
                g2.fill(new Rectangle2D.Float(rx, ry, width, height));
            }
            else{
                g2.fill(new Ellipse2D.Float(rx, ry, width, height));
            }

        }


    }




    public static class PassengerPainter implements ObjectPainter{

        protected int			dwidth;
        protected int			dheight;
        protected int			shape = 0; //0 for rectangle 1 for ellipse
        protected Map<String, Color> colourMap = new HashMap<String, Color>();
        protected boolean       darken;


        /**
         * Initializes painter
         */
        public PassengerPainter(int w, int h, int shape, boolean darken) {
            this.dwidth = w;
            this.dheight = h;
            this.shape = shape;
            this.darken = darken;
            this.initializeColours();
        }

        private void initializeColours(){
            colourMap.put(TaxiDomain.DARKGREY, Color.darkGray);
            colourMap.put(TaxiDomain.RED, Color.red);
            colourMap.put(TaxiDomain.GREEN, Color.green);
            colourMap.put(TaxiDomain.BLUE, Color.blue);
            colourMap.put(TaxiDomain.YELLOW, Color.yellow);
            colourMap.put(TaxiDomain.MAGENTA, Color.magenta);
            colourMap.put(TaxiDomain.PINK, Color.pink);
            colourMap.put(TaxiDomain.ORANGE, Color.orange);
            colourMap.put(TaxiDomain.CYAN, Color.cyan);
        }

        @Override
        public void paintObject(Graphics2D g2, OOState s, ObjectInstance ob, float cWidth, float cHeight) {

            String goalLocation = ((TaxiPassenger)ob).goalLocation;//getIntValForAttribute(TaxiDomain.GOALLOCATIONATT);
            int type = ((TaxiState)s).locationIndWithColour(goalLocation);

            String colour = ((TaxiPassenger)ob).goalLocation;

            Color col = darken? colourMap.get(colour).darker() : colourMap.get(colour);



            //set the color of the object
            g2.setColor(col);

            float domainXScale = this.dwidth;
            float domainYScale = this.dheight;

            //determine then normalized width
            float width = (1.0f / domainXScale) * cWidth;
            float height = (1.0f / domainYScale) * cHeight;

            float rx = ((TaxiPassenger)ob).x*width;
            float ry = cHeight - height - ((TaxiPassenger)ob).y*height;

            float rcx = rx + width/2f;
            float rcy = ry + height/2f;

            //now scale
            float sfactor = 0.8f;

            boolean inTaxi = ((TaxiPassenger)ob).inTaxi;
            if(inTaxi){
                sfactor = 0.5f;
            }

            float swidth = width*sfactor;
            float sheight = height*sfactor;

            float srx = rcx-(swidth/2f);
            float sry = rcy-(sheight/2f);

            if(this.shape == 0){
                g2.fill(new Rectangle2D.Float(srx, sry, swidth, sheight));
            }
            else{
                g2.fill(new Ellipse2D.Float(srx, sry, swidth, sheight));
            }

        }


    }



    public static class WallPainter implements ObjectPainter{

        int maxX;
        int maxY;
//        boolean vertical;


        public WallPainter(int w, int h){
            this.maxX = w;
            this.maxY = h;
//            this.vertical = vertical;
        }

        @Override
        public void paintObject(Graphics2D g2, OOState s, ObjectInstance ob,
                                float cWidth, float cHeight) {

            int p0x, p0y, p1x, p1y;

            int wp = ((TaxiMapWall)ob).wallOffset;
            int e1 = ((TaxiMapWall)ob).wallMin;
            int e2 = ((TaxiMapWall)ob).wallMax;
            boolean vertical  =((TaxiMapWall)ob).verticalWall;

            if(vertical){
                p0x = p1x = wp;
                p0y = e1;
                p1y = e2;
            }
            else{
                p0y = p1y = wp;
                p0x = e1;
                p1x = e2;
            }

            float nx0 = (float)p0x / (float)maxX;
            float ny0 = 1.f - ((float)p0y / (float)maxY);

            float nx1 = (float)p1x / (float)maxX;
            float ny1 = 1.f - ((float)p1y / (float)maxY);


            g2.setColor(Color.black);


            g2.setStroke(new BasicStroke(10));


            g2.drawLine((int)(nx0*cWidth), (int)(ny0*cHeight), (int)(nx1*cWidth), (int)(ny1*cHeight));


        }



    }

}
