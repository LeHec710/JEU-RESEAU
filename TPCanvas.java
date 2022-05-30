import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.BasicStroke;

/**
 * @author Hector BIDAN
 *
 */
public class TPCanvas extends Canvas {
	int size = 800;
	int nbPosition = 10;
	byte[] etat;

	Color primaryColor = new Color(45, 193, 159);
	Color lineColor = new Color(31, 143, 118);
	Color[] color = { Color.black, Color.white, new Color(38, 76, 87), Color.green, Color.red };
	// 0 black
	// 1 player a
	// 2 player b
	// 3 winners
	// 4 losers

	public TPCanvas(byte[] pEtat, int pNbPosition, int psize) {
		setBackground(primaryColor);
		this.etat = pEtat;
		nbPosition = pNbPosition;
		size = psize;
	}

	public void setEtat(byte[] petat) {
		this.etat = petat;
	}

	public byte[] getEtat() {
		return this.etat;
	}

	public void paint(Graphics win) {
		paintCarte(win);
		drawEtat(win);
	}

	public void paintCarte(Graphics win) {
		Graphics2D win2 = (Graphics2D) win;
		win2.setStroke(new BasicStroke(3));
		win2.setColor(lineColor);
		win2.drawRect(0, 0, size - 1, size - 1);
		for (int i = 1; i < nbPosition; i++) {
			win2.drawLine(i * size / nbPosition, 0, i * size / nbPosition, size);
			win2.drawLine(0, i * size / nbPosition, size, i * size / nbPosition);
		}
	}

	public void drawEtat(Graphics win) {
		for (int i = 0; i < nbPosition * nbPosition; i++) {
			if (etat[2 * i] != 0) {
				// System.out.println("Joueur "+etat[2*i]+ " X "+i%10+" Y "+i/10);
				drawPlayer(win, i % nbPosition, i / nbPosition, etat[2 * i + 1]);
			}
		}
	}

	public void drawPlayer(Graphics win, int x, int y, byte type) {
		win.setColor(color[type]);
		int padding = (size / nbPosition) / 6;
		win.fillOval(
				((x * size / nbPosition) + 1) + padding / 2,
				((y * size / nbPosition) + 1) + padding / 2,
				(size / nbPosition - 1) - padding,
				(size / nbPosition - 1) - padding
		);

		padding *= 4;
		win.setColor(new Color(0, 0, 0, 50));
		win.fillOval(
				((x * size / nbPosition) + 1) + padding / 2,
				((y * size / nbPosition) + 1) + padding / 2,
				(size / nbPosition - 1) - padding,
				(size / nbPosition - 1) - padding
		);			
	}
}
