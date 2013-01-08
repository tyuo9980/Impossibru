// by Peter Li
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
// v1.01 - 5/11/12
// - Improved collision detection
// - Added pits
// - Efficiency improvements
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
// v1.0 - 5/10/12
// - Initial Release
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

import java.awt.*;
import java.awt.font.*;
import java.awt.geom.*;
import java.text.*;
import java.io.*;
import javax.imageio.*;

public class Impossibru
{
    //console vars
    public static PCPC c;
    public static Graphics g;
    public static Graphics2D g2d;
    public static int cWidth, cHeight;
    public static int mX, mY;
    public static boolean mClick;
    public static String command;
    public static int key, keyR;

    //object vars
    public static int pX;
    public static double pY;
    public static int bX[] = new int [1337];
    public static int bY[] = new int [1337];
    public static int sX[] = new int [1337];
    public static int sY[] = new int [1337];
    public static int pitPos[];
    public static int pitLen[];
    public static boolean boxOnScreen[] = new boolean [1337];
    public static boolean spkOnScreen[] = new boolean [1337];
    public static boolean pitOnScreen[];
    public static int lX, lY;
    public static Rectangle pl, bx, sp, sp2;

    //game vars
    public static char map[] [];
    public static int boxes;
    public static int spikes;
    public static int pits;
    public static int attempts;
    public static int completion;
    public static boolean jump, land;
    public static boolean quit, back;
    public static double velo, gvty;

    //image vars
    public static AffineTransform affineTransform = new AffineTransform ();
    public static Image bg;
    public static Image player;
    public static Image menu;
    public static Image stat;

    //misc vars
    public static boolean cb, drawfps, drawplayer;
    public static String string;
    public static double counter;
    public static Color triFill = new Color (26, 76, 82);
    public static String soundtrack = "audio\\heaven.wav";
    public static String level = "level1.map";

    //text vars
    public static Font font, font2;
    public static AttributedString as, as2;

    //timer vars
    public static int fps = 30; //33
    public static double delay = 1000 / fps;
    public static long mainTimer, inputTimer, fpsTimer, timer, timer2, gameTimer;

    public static void main (String args[]) throws Exception
    {
	c = new PCPC ();
	g = c.getGraphics ();
	g2d = (Graphics2D) g;
	cWidth = c.getCanvasWidth ();
	cHeight = c.getCanvasHeight ();

	//loads images
	bg = ImageIO.read (new File ("images\\background2.jpg"));
	player = ImageIO.read (new File ("images\\player.jpg"));
	menu = ImageIO.read (new File ("images\\menu.jpg"));
	stat = ImageIO.read (new File ("images\\stats.jpg"));

	//reads # of attempts from file
	attempts = Integer.parseInt (c.readFile ("fails.num", 1));
	completion = Integer.parseInt (c.readFile ("fails.num", 2));

	help ();
	menu ();
    }


    public static void help ()
    {
	System.out.println ("The Impossible Game clone");
	System.out.println ("by Peter Li");
	System.out.println ("5/7/12");
	System.out.println ("");
	System.out.println ("To use commands, type the");
	System.out.println ("command in the textbox above");
	System.out.println ("and press enter.");
	System.out.println ("");
	System.out.println ("Command list:");
	System.out.println ("quit                   - exits to menu");
	System.out.println ("locate               - outputs player coords");
	System.out.println ("drawplayer      - draws player coords");
	System.out.println ("boxnum           - outputs # of boxes");
	System.out.println ("spikenum       - outputs # of spikes");
	System.out.println ("fps 'x'                - sets FPS value");
	System.out.println ("drawfps           - shows fps counter");
	System.out.println ("respawn          - restart level");
	System.out.println ("reset                 - resets attempts");
    }


    public static void menu () throws Exception
    {
	while (true)
	{
	    //draws menu image
	    g.drawImage (menu, 0, 0, null);

	    //gets mouse info
	    mX = c.getMouseX ();
	    mY = c.getMouseY ();
	    mClick = c.getClick ();

	    //start button click detection
	    if (mX > 290 && mX < 495 && mY > 265 && mY < 370)
	    {
		if (mClick)
		{
		    mClick = false;
		    reset ();
		    readMap (level);
		    start ();
		}
	    }
	    //stats button click detection
	    else if (mX > 290 && mX < 495 && mY > 400 && mY < 500)
	    {
		if (mClick)
		{
		    mClick = false;
		    back = false;
		    stats ();
		}
	    }

	    //updates screen
	    c.ViewUpdate ();
	    c.delay (10);
	}
    }


    public static void stats () throws Exception
    {
	while (!back)
	{
	    //gets mouse info
	    mX = c.getMouseX ();
	    mY = c.getMouseY ();
	    mClick = c.getClick ();

	    //draws background image
	    g.drawImage (stat, 0, 0, null);

	    //outputs # of attempts in diff font
	    font = Font.createFont (Font.PLAIN, new FileInputStream (new File ("loaded.ttf")));
	    font = font.deriveFont (Font.PLAIN, 20);
	    g.setFont (font);

	    as = new AttributedString ("Total attempts: " + attempts);
	    as.addAttribute (TextAttribute.FONT, font);
	    g.drawString (as.getIterator (), 270, 300);

	    as = new AttributedString ("Completed on attempt: " + completion);
	    as.addAttribute (TextAttribute.FONT, font);
	    g.drawString (as.getIterator (), 230, 340);

	    font = font.deriveFont (Font.PLAIN, 26);
	    g.setFont (font);

	    //back button
	    as = new AttributedString ("Back");
	    as.addAttribute (TextAttribute.FONT, font);
	    g.drawString (as.getIterator (), 347, 582);

	    //back button click detection
	    if (mX > 307 && mX < 459 && mY > 536 && mY < 612)
	    {
		if (mClick)
		{
		    mClick = false;
		    back = true;
		}
	    }

	    //updates screen
	    c.ViewUpdate ();
	    c.delay (10);
	}
    }


    //main game
    public static void start () throws Exception
    {
	String ss;
	attempts++;
	c.writeFile (attempts + "\n" + completion, "fails.num");
	c.playAudio (soundtrack);

	mainTimer = System.currentTimeMillis ();
	fpsTimer = mainTimer;

	while (!quit)
	{
	    //loop control timer
	    timer = System.currentTimeMillis ();
	    if (timer - mainTimer > delay)
	    {
		mainTimer = timer;

		getInput ();                                //gets user input
		commands ();                                //command console
		winCheck ();                                //checks if level is complete
		objCheck ();                                //checks on screen objects
		drawMap ();                                 //draws background
		drawPit ();                                 //draws pits
		drawBox ();                                 //draws boxes
		drawSpike ();                               //draws spikes
		drawPlayer ();                              //draws player
		drawAttempts ();                            //draws # of attempts

		//fps counter
		if (drawfps)
		{
		    timer2 = System.currentTimeMillis ();
		    ss = Integer.toString ((int) (1000 / (timer2 - fpsTimer)));
		    fpsTimer = timer2;
		    g.setColor (Color.white);
		    g.drawString (ss, 50, 100);
		}

		c.ViewUpdate ();                            //updates screen
		//c.delay (22);
	    }
	}
    }


    public static void reset () throws Exception
    {
	quit = false;
	land = true;
	boxes = 0;
	spikes = 0;
	pits = 0;
	lX = 140;
	lY = 510;
	pX = 0;
	pY = 509;
	c.writeFile (attempts + "\n" + completion, "fails.num");
    }


    public static void commands () throws Exception
    {
	command = c.getText ();
	String sss[] = command.split (" ");

	if (command.equals ("quit"))
	{
	    quit = true;
	    c.stopAudio ();
	}
	else if (command.equals ("locate"))
	{
	    System.out.println (pX + " " + pY);
	}
	else if (command.equals ("drawfps"))
	{
	    if (drawfps)
	    {
		drawfps = false;
	    }
	    else
	    {
		drawfps = true;
	    }
	}
	else if (sss [0].equals ("fps"))
	{
	    fps = Integer.parseInt (sss [1]);
	    delay = 1000 / fps;
	}
	else if (sss [0].equals ("boxnum"))
	{
	    System.out.println (boxes);
	}
	else if (sss [0].equals ("spikenum"))
	{
	    System.out.println (land);
	}
	else if (command.equals ("drawplayer"))
	{
	    if (drawplayer)
	    {
		drawplayer = false;
	    }
	    else
	    {
		drawplayer = true;
	    }
	}
	else if (command.equals ("respawn"))
	{
	    respawn ();
	}
	else if (command.equals ("reset"))
	{
	    c.writeFile ("0\n0", "fails.num");
	    attempts = 0;
	    respawn ();
	}
    }


    public static void objCheck ()
    {
	//boxes
	for (int b = 1 ; b <= boxes ; b++)
	{
	    if (bX [b] > -30 && bX [b] < cWidth)
	    {
		boxOnScreen [b] = true;
	    }
	    else
	    {
		boxOnScreen [b] = false;
	    }
	}

	//spikes
	for (int s = 1 ; s <= spikes ; s++)
	{
	    if (sX [s] > -30 && sX [s] < cWidth)
	    {
		spkOnScreen [s] = true;
	    }
	    else
	    {
		spkOnScreen [s] = false;
	    }
	}

	//pits
	for (int p = 1 ; p <= pits ; p++)
	{
	    if (pitPos [p] + pitLen [p] > 0 && pitPos [p] < cWidth)
	    {
		pitOnScreen [p] = true;
	    }
	    else
	    {
		pitOnScreen [p] = false;
	    }
	}
    }


    public static void winCheck () throws Exception
    {
	if (pX > 25400)
	{
	    completion = attempts;
	    c.writeFile (attempts + "\n" + completion, "fails.num");
	    g.drawString ("Level 1 Complete", 300, 330);
	    g.drawString ("It took you long enough", 190, 370);
	    c.ViewUpdate ();
	    c.delay (5000);
	    quit = true;
	    c.stopAudio ();
	    stats ();
	}
    }


    public static void readMap (String lvl) throws Exception
    {
	//reads map dimensions and # of pits
	String s[] = c.readFile (lvl, 1).split (" ");
	int p = Integer.parseInt (s [0]);
	int x = Integer.parseInt (s [1]);
	int y = Integer.parseInt (s [2]);
	map = new char [x + 1] [y + 1];
	pitPos = new int [p + 1];
	pitLen = new int [p + 1];
	pitOnScreen = new boolean [p + 1];
	pits = p;

	//reads file and sets object coords
	for (int b = 1 ; b <= y ; b++)
	{
	    string = c.readFile (lvl, b + 1);
	    for (int a = 1 ; a <= x ; a++)
	    {
		map [a] [b] = string.charAt (a - 1);
		if (map [a] [b] == '1')
		{
		    boxes++;
		    bX [boxes] = 2000 + a * 30;
		    bY [boxes] = 509 - (y - b) * 30;
		}
		else if (map [a] [b] == '2')
		{
		    spikes++;
		    sX [spikes] = 2000 + a * 30;
		    sY [spikes] = 509 - (y - b) * 30;
		}
	    }
	}

	//sets pit positions
	for (int a = 1 ; a <= p ; a++)
	{
	    s = c.readFile (lvl, y + 1 + a).split (" ");
	    pitPos [a] = 2000 + Integer.parseInt (s [0]) * 30;
	    pitLen [a] = Integer.parseInt (s [1]) * 30;
	}
    }


    public static void drawMap ()
    {
	g.drawImage (bg, 0, 0, null);
	g.setColor (Color.white);
	g.drawLine (lX, lY, lX + 500, lY);
    }


    public static void drawPlayer () throws Exception
    {
	pX += 12;

	if (!land)
	{
	    //gravity
	    velo -= 4;
	    counter += 15;
	    pY -= velo;

	    //collision detection
	    collision ();

	    //rotation animation
	    rotate (player, counter);
	    g2d.drawImage (player, affineTransform, null);
	}
	else
	{
	    g.drawImage (player, 200, (int) pY - 29, null);

	    //collision detection
	    collision ();
	}

	if (jump)
	{
	    if (land)
	    {
		land = false;
		counter = 0;
		velo = 25;
	    }
	}

	if (drawplayer)
	{
	    g.setColor (Color.red);
	    g.drawRect (200, (int) pY - 30, 30, 30);
	}
    }


    public static void drawBox ()
    {
	for (int i = 1 ; i <= boxes ; i++)
	{
	    bX [i] -= 12;
	    if (boxOnScreen [i])
	    {
		g.setColor (Color.white);
		g.drawRect (bX [i], bY [i] - 29, 30, 30);
		g.setColor (Color.black);
		g.fillRect (bX [i] + 1, bY [i] - 28, 29, 29);
	    }
	}
    }


    public static void drawSpike ()
    {
	for (int i = 1 ; i <= spikes ; i++)
	{
	    sX [i] -= 12;
	    if (spkOnScreen [i])
	    {
		//creates triangle polygon
		int triX[] = {sX [i], sX [i] + 15, sX [i] + 30};
		int triY[] = {sY [i] + 1, sY [i] - 29, sY [i] + 1};

		g.setColor (triFill);
		g.fillPolygon (triX, triY, 3);
		g.setColor (Color.white);
		g.drawPolygon (triX, triY, 3);
	    }
	}
    }


    public static void drawPit ()
    {
	for (int i = 1 ; i <= pits ; i++)
	{
	    pitPos [i] -= 12;
	    if (pitOnScreen [i])
	    {
		g.setColor (Color.black);
		g.fillRect (pitPos [i], 509, pitLen [i], 15);
	    }
	}
    }


    public static void drawAttempts () throws Exception
    {
	//outputs # of attempts on screen
	g.setColor (Color.white);

	font2 = Font.createFont (Font.PLAIN, new FileInputStream (new File ("loaded.ttf")));
	font2 = font2.deriveFont (Font.PLAIN, 24);
	g.setFont (font2);

	as2 = new AttributedString (Integer.toString (attempts));
	as2.addAttribute (TextAttribute.FONT, font2);
	g.drawString (as2.getIterator (), 423, 135);
    }


    public static void collision () throws Exception
    {
	pl = new Rectangle (200, (int) pY - 30, 30, 30);
	land = false;

	//hits ground
	if (pY >= 509)
	{
	    land = true;
	    pY = 509;
	}

	//collides with boxes
	for (int b = 1 ; b <= boxes ; b++)
	{
	    if (boxOnScreen [b])
	    {
		bx = new Rectangle (bX [b], bY [b] - 30, 30, 30);

		if (pl.intersects (bx))
		{
		    if (pY < bY [b] && bX [b] > 170 && bX [b] < 230)
		    {
			land = true;
			pY = bY [b] - 29;
			velo = 0;
			break;
		    }
		    else
		    {
			respawn ();
			break;
		    }
		}
	    }
	}

	//collides with spikes
	for (int s = 1 ; s <= spikes ; s++)
	{
	    if (spkOnScreen [s])
	    {
		sp = new Rectangle (sX [s], sY [s] - 2, 30, 1);                 //horizontal
		sp2 = new Rectangle (sX [s] + 15, sY [s] - 30, 1, 30);          //vertical

		if (pl.intersects (sp) || pl.intersects (sp2))
		{
		    respawn ();
		    break;
		}
	    }
	}


	//collides with pits
	for (int p = 1 ; p <= pits ; p++)
	{
	    if (pitOnScreen [p])
	    {
		if (pY == 509 && pitPos [p] < 200 && pitPos [p] + pitLen [p] > 200)
		{
		    respawn ();
		    break;
		}
	    }
	}
    }


    public static void respawn () throws Exception
    {
	c.stopAudio ();
	c.playAudio ("audio\\fail.wav");
	c.delay (250);
	attempts++;
	reset ();
	readMap (level);
	c.stopAudio ();
	c.playAudio (soundtrack);
    }


    public static void getInput ()
    {
	//gets input
	key = c.getKey ();
	keyR = c.getKeyR ();
	mClick = c.getClick ();

	if (key == 32)
	{
	    jump = true;
	}
	else if (keyR == 32)
	{
	    jump = false;
	}

	//menu button click detection

	if (mClick)
	{
	    mX = c.getMouseX ();
	    mY = c.getMouseY ();
	    if (mX > 30 && mX < 92 && mY > 20 && mY < 57)
	    {
		mClick = false;
		quit = true;
		c.stopAudio ();
	    }
	}
    }


    //player rotation function
    public static void rotate (Image image, double angle)
    {
	affineTransform.setToTranslation (200, pY - 30);
	affineTransform.rotate (Math.toRadians (angle), 15, 15);
    }
}
