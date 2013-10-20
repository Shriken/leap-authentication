import java.lang.InterruptedException;
import com.leapmotion.leap.*;
import java.io.IOException;
import java.io.FileWriter;
import java.util.Scanner;
import java.io.File;

public class Recorder {

	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);

		System.out.println("Are you righthanded? (y/n) ");
		String response = sc.next();
		sc.nextLine();
		boolean rightHanded = (response == "y");

		System.out.println("Enter the filename. ");
		String fn = sc.nextLine();

		System.out.println("Enter the number of patterns to store. ");
		int patterns = sc.nextInt();
		sc.nextLine();

		for (int i=0; i<patterns; i++) {
			Pattern p = recordPattern(rightHanded);
			if (patterns > 1)
				savePattern(p, fn + i);
			else
				savePattern(p, fn);
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

	public static void savePattern(Pattern pattern, String fn) {
		try {
			fn = "recordings/" + fn;

			FileWriter fw = new FileWriter(new File(fn));

			System.out.println("Saving pattern");

			fw.write(pattern.rightHanded + "\n");

			//for each frame in the pattern
			for (int i=0; i<pattern.length; i++) {
				if (i > 20)
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

}
