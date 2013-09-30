import java.lang.InterruptedException;
import com.leapmotion.leap.*;
import java.io.IOException;
import java.io.FileWriter;
import java.util.Scanner;
import java.io.File;

public class Tester {

	public static void main(String[] args) {
		String fn1 = getInput("Enter a fileset to load ");
		int patterns = Integer.parseInt(getInput("How many patterns are there?"));

		Pattern[] pa = new Pattern[patterns];
		for (int i=0; i<patterns; i++)
			pa[i] = getSavedPattern(fn1 + i);

		boolean rightHanded = ("y" == getInput("Are you righthanded? (y/n) "));


		/*  Test Loop */

		while (true) {
			Pattern test = recordPattern(rightHanded);

			double score = 0;
			for (Pattern p : pa)
				score += p.compare(test);

			System.out.println("Score: " + score);
			int totalFrames = 0;
			for (Pattern p : pa)
				totalFrames += p.length;

			System.out.println("Score per frame: " + score / totalFrames);
			double scorePerFinger = score / totalFrames / 6;
			System.out.println("Score per finger per frame: " + scorePerFinger);

			System.out.println();
			if (scorePerFinger < 60) {
				System.out.println("Welcome! Success!");
			} else {
				System.out.println("You fail.");
			}
		}
	}

	public static Pattern recordPattern(boolean rightHanded) {
		Controller controller = new Controller();
		CustomListener listener = new CustomListener(rightHanded);

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

	public static String getInput(String prompt) {
		System.out.println(prompt);
		
		Scanner sc = new Scanner(System.in);
		return sc.nextLine();
	}

	public static Pattern getSavedPattern(String fn) {
		try {
			fn = "recordings/" + fn;
			Scanner sc = new Scanner(new File(fn));

			Pattern p = new Pattern(sc.nextBoolean());
			sc.nextLine();

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
