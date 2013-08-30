import com.leapmotion.leap.*;

public class CustomListener extends Listener {

	Pattern pattern;
	boolean recording;
	boolean done;
	boolean fin;
	int frameCount;

	int framesNotMoving;
	final int RECORDING_TIMEOUT = 50; //???

	public void onInit(Controller controller) {
		System.out.println("Initialized");

		pattern = new Pattern();
		recording = false;
		done = false;

		framesNotMoving = 0;
		frameCount = 0;
	}

	public void onFrame(Controller controller) {
		Frame frame = controller.frame();

		if (recording) {
			pattern.addFrame(frame);

			if (frameCount > 1 && pattern.justMoved())
				framesNotMoving = 0;
			else
				framesNotMoving++;

			if (framesNotMoving > RECORDING_TIMEOUT) {
				done = true;
				recording = false;
				System.out.println("done");
			}

			frameCount++;
		} else if (inStartPosition(frame) && !done)
			recording = true;
	}

	public boolean inStartPosition(Frame frame) {
		if (frame.hands().count() != 1)
			return false;

		Hand hand = frame.hands().get(0);

		if (hand.fingers().count() != 5)
			return false;

		if (hand.tools().count() != 0)
			return false;

		float pitch = hand.direction().pitch();
		float roll = hand.direction().roll();

		if (-20 < pitch && pitch < 20 && -20 < roll && roll < 20) {
			return true;
		} else
			return false;
	}


	public void onConnect(Controller controller) {
		System.out.println("Connected");
	}

	public void onDisconnect(Controller controller) {
		System.out.println("Disconnected");
	}

	public void onExit(Controller controller) {
		System.out.println("Exited");
	}
}
