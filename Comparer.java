import java.io.File;
import java.util.Scanner;
import java.io.IOException;
import com.leapmotion.leap.*;

public class Comparer {

	public static void main(String[] argrs) {
		String fn1 = getInput("Enter a fileset to load ");

		Scanner sc = new Scanner(System.in);
		int patterns = sc.nextInt();

		Pattern[] pa = new Pattern[patterns];
		for (int i=0; i<patterns; i++)
			pa[i] = getSavedPattern(fn1 + i);

		String fn2 = getInput("Enter a file to compare to ");
		Pattern q = getSavedPattern(fn2);

		double score = 0;
		for (Pattern p : pa)
			score += p.compare(q);

		System.out.println("Score: " + score);
	}

	public static String getInput(String prompt) {
		System.out.println(prompt);
		
		Scanner sc = new Scanner(System.in);
		return "recordings/" + sc.nextLine();
	}

	public static Pattern getSavedPattern(String fn) {
		try {
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
