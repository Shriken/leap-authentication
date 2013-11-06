import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import com.leapmotion.leap.*;
import javax.swing.JButton;
import javax.swing.JFrame;
import java.awt.Dimension;
import java.awt.Color;

public class Tester {

	static final int RECORDS_PER_PERSON = 5;
	static final int SELF_AUTHS_PER_PERSON = 5;
	static final int MASTER_AUTHS_PER_PERSON = 5;
	static final int LATE_AUTHS_PER_PERSON = 5;

	static Tester instance;

	boolean set;
	boolean rightHanded;

	Pattern[] masterPatterns;

	Controller controller;
	TestComponent tc;
	JFrame frame;

	public Tester() {
		frame = new JFrame("Tester");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		tc = new TestComponent(800, 600);
		controller = new Controller();
		frame.add(tc);

		frame.pack();
		frame.setVisible(true);

		masterPatterns = new Pattern[5];
		for (int i=0; i<5; i++)
			masterPatterns[i] = Comparer.getSavedPattern("master_" + i);

		instance = this;
		set = false;
	}

	public void test() {
		rightHanded = true;

		//record patterns
		Pattern[] patterns = new Pattern[RECORDS_PER_PERSON];
		for (int i=0; i<RECORDS_PER_PERSON; i++)
			patterns[i] = recordPattern(rightHanded);

		//try to auth with those patterns
		Pattern[] selfAuths = new Pattern[SELF_AUTHS_PER_PERSON];
		double[] selfAuthScores = new double[SELF_AUTHS_PER_PERSON];
		for (int i=0; i<SELF_AUTHS_PER_PERSON; i++) {
			selfAuths[i] = recordPattern(rightHanded);
			selfAuthScores[i] = authenticate(selfAuths[i], patterns);
			tc.displayScore(selfAuthScores[i]);
			sleep(2000);
		}

		//try to auth with master pattern
		Pattern[] masterAuths = new Pattern[MASTER_AUTHS_PER_PERSON];
		double[] masterAuthScores = new double[MASTER_AUTHS_PER_PERSON];
		for (int i=0; i<MASTER_AUTHS_PER_PERSON; i++) {
			masterAuths[i] = recordPattern(rightHanded);
			masterAuthScores[i] = authenticate(masterAuths[i],
											   masterPatterns);
			tc.displayScore(masterAuthScores[i]);
			sleep(2000);
		}

		//try to auth as self again
		Pattern[] lateAuths = new Pattern[LATE_AUTHS_PER_PERSON];
		double[] lateAuthScores = new double[LATE_AUTHS_PER_PERSON];
		for (int i=0; i<LATE_AUTHS_PER_PERSON; i++) {
			lateAuths[i] = recordPattern(rightHanded);
			lateAuthScores[i] = authenticate(lateAuths[i], patterns);
			tc.displayScore(lateAuthScores[i]);
			sleep(2000);
		}
	}

	public Pattern recordPattern(boolean rightHanded) {
		CustomListener listener = new CustomListener(rightHanded);
		controller.addListener(listener);

		tc.displayString("Place hand flat above the Leap", Color.YELLOW);
		while (!listener.recording)
			sleep(70);

		tc.displayString("Perform gesture", Color.GREEN);
		while (listener.recording)
			sleep(70);

		controller.removeListener(listener);
		return listener.pattern;
	}

	public double authenticate(Pattern auth, Pattern[] patterns) {
		double score = 0;
		int totalFrames = 0;

		for (int i=0; i<patterns.length; i++) {
			score 		+= auth.compare(patterns[i]);
			totalFrames += patterns[i].length;
		}

		return score / totalFrames;
	}

	public void sleep(int ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		Tester tester = new Tester();
		tester.test();
	}
}
