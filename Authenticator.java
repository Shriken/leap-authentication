import java.lang.InterruptedException;
import com.leapmotion.leap.*;
import java.io.IOException;
import java.io.FileWriter;
import java.util.Scanner;
import java.io.File;

public class Authenticator {

	public static void main(String[] args) {
		Pattern p = recordPattern();

		savePattern(p);

		Pattern q = getSavedPattern();

		double[] score = p.compare(q);

		System.out.println(score[0]);
	}

	public static Pattern recordPattern() {
		Controller controller = new Controller();
		CustomListener listener = new CustomListener();

		controller.addListener(listener);

		while (!listener.done) {
			try {
				Thread.sleep(70);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		System.out.println("asdf");

		controller.removeListener(listener);

		listener.pattern.normalize();

		return listener.pattern;
	}

	public static void savePattern(Pattern pattern) {
		try {
			Scanner sc = new Scanner(System.in);
			System.out.println("Enter a filename");

			String fn = sc.nextLine();

			FileWriter fw = new FileWriter(new File(fn));

			System.out.println("Saving pattern");

			for (int i=0; i<pattern.length; i++) {
				if (i > 30)
					fw.flush();

				//write palm
				Vector[] palm = pattern.palmData.get(i);
				String s = "p ";

				//position
				s += palm[0].getX() + " ";
				s += palm[0].getY() + " ";
				s += palm[0].getZ() + " ";

				//direction
				s += palm[1].getX() + " ";
				s += palm[1].getY() + " ";
				s += palm[1].getZ();

				fw.write(s + "\n");

				//write fingers
				for (Vector[] finger : pattern.fingerData.get(i)) {

					if (finger[0] != null) {
						s = "1 ";

						//position
						s += finger[0].getX() + " ";
						s += finger[0].getY() + " ";
						s += finger[0].getZ() + " ";

						//direction
						s += finger[1].getX() + " ";
						s += finger[1].getY() + " ";
						s += finger[1].getZ();
					} else {
						s = "-1 ";
					}

					fw.write(s + "\n");
				}

				fw.write("\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static Pattern getSavedPattern() {
		try {
			Scanner fnReader = new Scanner(System.in);
			System.out.println("Enter a file to compare to");

			String fn = fnReader.nextLine();

			Scanner sc = new Scanner(new File(fn));

			Pattern p = new Pattern();

			while (sc.hasNextLine()) {
				sc.next(); //get rid of leading 'p'

				//load the palm data
				Vector palmPosition = new Vector();
				palmPosition.setX((float)sc.nextDouble());
				palmPosition.setY((float)sc.nextDouble());
				palmPosition.setZ((float)sc.nextDouble());

				Vector palmDirection = new Vector();
				palmDirection.setX((float)sc.nextDouble());
				palmDirection.setY((float)sc.nextDouble());
				palmDirection.setZ((float)sc.nextDouble());

				Vector[] palmData = new Vector[2];
				palmData[0] = palmPosition;
				palmData[1] = palmDirection;

				sc.nextLine();

				Vector[][] handData = new Vector[5][2];

				for (int i=0; i<5; i++) {
					Vector[] fingerData = new Vector[2];

					int flag = sc.nextInt();

					if (flag != -1) {
						Vector tipPosition = new Vector();
						tipPosition.setX((float)sc.nextDouble());
						tipPosition.setY((float)sc.nextDouble());
						tipPosition.setZ((float)sc.nextDouble());

						Vector tipDirection = new Vector();
						tipDirection.setX((float)sc.nextDouble());
						tipDirection.setY((float)sc.nextDouble());
						tipDirection.setZ((float)sc.nextDouble());

						fingerData[0] = tipPosition;
						fingerData[1] = tipPosition;
					}

					handData[i] = fingerData;
					sc.nextLine();
				}

				sc.nextLine();

				p.addFrameData(palmData, handData);
			}

			return p;
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}
}
