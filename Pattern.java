import java.util.ArrayList;
import com.leapmotion.leap.*;

public class Pattern {

	ArrayList<Vector[][]> fingerData;
	ArrayList<Vector[]> palmData;
	int[] fingerKey;
	int length; //number of stored frames

	final double MOVEMENT_THRESHOLD = 50; //????????

	public Pattern() {
		fingerData = new ArrayList<Vector[][]>();
		palmData = new ArrayList<Vector[]>();
		fingerKey = new int[5];
		length = 0;
	}

	public double[] compare(Pattern p) {
		//doesn't account for time differences... is that good?
		double[] score = new double[2];

		for (int i=0; i<p.length && i<length; i++) {
			double[] cf = compareFrames(p, i);
			score[0] += cf[0];
			score[1] += cf[1];
		}

		return score;
	}

	public boolean justMoved() {
		//compare the two most recent frames, check if there was movement

		double[] scores = compareFrames(length-1, length-2);

		System.out.println(scores[0]);

		return scores[0] > MOVEMENT_THRESHOLD; // || scores[1] > ROTATION_THRESHOLD;
	}

	public double movement() {
		if (length > 1)
			return compareFrames(length-1, length-2)[0];
		else
			return 0;
	}

	public void addFrame(Frame frame) {
		Hand hand = frame.hands().get(0);

		//record palm data
		Vector[] palm = new Vector[2];
		palm[0] = hand.palmPosition();
		palm[1] = hand.direction();

		palmData.add(palm);
		
		if (length == 0) {
			Vector[][] fingers = sortByX(hand.fingers());

			fingerData.add(fingers);
		} else {
			Finger[] fingers = new Finger[5];
			ArrayList<Finger> unboundFingers = new ArrayList<Finger>();
			int countUnbounds = 5;

			//bind fingers or mark as unbound
			for (Finger f : hand.fingers()) {
				boolean unbound = true;

				for (int i=0; i<5; i++)
					if (fingerKey[i] == f.id()) {
						fingers[i] = f;
						unbound = false;
						countUnbounds--;
					}
				
				if (unbound)
					unboundFingers.add(f);
			}

			//rebind unbound fingers
			if (countUnbounds == 1 && unboundFingers.size() == 1) {
				for (int i=0; i<5; i++)
					if (fingers[i] == null)
						fingers[i] = unboundFingers.get(0);
			} else {
				//put the fingers in the most appropriate spots
				for (Finger f : unboundFingers)
					for (int i=0; i<5; i++)
						if (fingers[i] == null)
							fingers[i] = f;
			}

			Vector[][] vFingers = new Vector[5][2];
			
			for (int i=0; i<5; i++) {
				if (fingers[i] == null) {
					vFingers[i][0] = null;
					vFingers[i][1] = null;
				} else {
					vFingers[i][0] = fingers[i].tipPosition();
					vFingers[i][1] = fingers[i].direction();
				}
			}

			fingerData.add(vFingers);
		}

		length++; 
	}

	public void addFrameData(Vector[] palm, Vector[][] hand) {
		palmData.add(palm);
		fingerData.add(hand);
		length++;
	}

	public Vector[][] sortByX(FingerList fl) {
		Finger[] fingers = new Finger[5];

		for (int i=0; i<5; i++)
			fingers[i] = fl.get(i);

		//sort the fingers by X coodinate
		for (int i=4; i>0; i--)
			for (int j=0; j<i; j++)
				if (fingers[j].tipPosition().getX() > fingers[j+1].tipPosition().getX()) {
					Finger temp = fingers[j];
					fingers[j] = fingers[j+1];
					fingers[j+1] = temp;
				}

		for (int i=0; i<5; i++)
			fingerKey[i] = fingers[i].id();

		Vector[][] fingerDatums = new Vector[5][2];
		for (int i=0; i<5; i++) {
			fingerDatums[i][0] = fingers[i].tipPosition();
			fingerDatums[i][1] = fingers[i].direction();
		}

		return fingerDatums;
	}

	//normalize with respect to our intial palm position
	public void normalize() {
		Vector initPos = palmData.get(0)[0];

		for (int i=1; i<length; i++) { //skip first frame
			//normalize palm
			Vector framePos = palmData.get(i)[0];

			framePos.setX(framePos.getX() - initPos.getX());
			framePos.setY(framePos.getY() - initPos.getY());
			framePos.setZ(framePos.getZ() - initPos.getZ());

			//normalize fingers
			for (int j=0; j<5; j++) {
				Vector fingerPos = fingerData.get(i)[j][0];

				if (fingerPos != null) {
					fingerPos.setX(fingerPos.getX() - initPos.getX());
					fingerPos.setY(fingerPos.getY() - initPos.getY());
					fingerPos.setZ(fingerPos.getZ() - initPos.getZ());
				}
			}
		}

		Vector framePos = palmData.get(0)[0];

		//set first frame
		palmData.get(0)[0].setX(framePos.getX() - initPos.getX());
		palmData.get(0)[0].setY(framePos.getY() - initPos.getY());
		palmData.get(0)[0].setZ(framePos.getZ() - initPos.getZ());

		//normalize fingers
		for (int i=0; i<5; i++) {
			Vector fingerPos = fingerData.get(0)[i][0];

			if (fingerPos != null) {
				fingerPos.setX(fingerPos.getX() - initPos.getX());
				fingerPos.setY(fingerPos.getY() - initPos.getY());
				fingerPos.setZ(fingerPos.getZ() - initPos.getZ());
			}
		}
	}

	public double[] compareFrames(Pattern p, int frameNum) {
		double[] score = new double[2];

		double directionScore = 0;
		double positionScore = 0;

		for (int i=0; i<5; i++) {
			Vector[] f = fingerData.get(frameNum)[i];
			Vector[] pf = p.fingerData.get(frameNum)[i];

			if (f[0] != null && pf[0] != null) {
				positionScore -= Math.abs(f[0].getX() - pf[0].getX());
				positionScore -= Math.abs(f[0].getY() - pf[0].getY());
				positionScore -= Math.abs(f[0].getZ() - pf[0].getZ());

				directionScore -= Math.abs(f[1].pitch() - pf[1].pitch());
				directionScore -= Math.abs(f[1].roll() - pf[1].roll());
				directionScore -= Math.abs(f[1].yaw() - pf[1].yaw());
			}
		}

		Vector[] f = palmData.get(frameNum);
		Vector[] pf = p.palmData.get(frameNum);

		positionScore -= Math.abs(f[0].getX() - pf[0].getX());
		positionScore -= Math.abs(f[0].getY() - pf[0].getY());
		positionScore -= Math.abs(f[0].getZ() - pf[0].getZ());

		directionScore -= Math.abs(f[1].pitch() - pf[1].pitch());
		directionScore -= Math.abs(f[1].roll() - pf[1].roll());
		directionScore -= Math.abs(f[1].yaw() - pf[1].yaw());

		return score;
	}

	public double[] compareFrames(int fn1, int fn2) {
		double[] score = new double[2];

		double positionScore = 0;
		double directionScore = 0;

		for (int i=0; i<5; i++) {
			if (fingerData.get(fn1)[i][0] != null && fingerData.get(fn2)[i][0] != null) {
				Vector[] f = fingerData.get(fn1)[i];
				Vector[] pf = fingerData.get(fn2)[i];

				positionScore -= Math.abs(f[0].getX() - pf[0].getX());
				positionScore -= Math.abs(f[0].getY() - pf[0].getY());
				positionScore -= Math.abs(f[0].getZ() - pf[0].getZ());

				directionScore -= Math.abs(f[1].pitch() - pf[1].pitch());
				directionScore -= Math.abs(f[1].roll() - pf[1].roll());
				directionScore -= Math.abs(f[1].yaw() - pf[1].yaw());
			}
		}

		Vector[] f = palmData.get(fn1);
		Vector[] pf = palmData.get(fn2);

		positionScore -= Math.abs(f[0].getX() - pf[0].getX());
		positionScore -= Math.abs(f[0].getY() - pf[0].getY());
		positionScore -= Math.abs(f[0].getZ() - pf[0].getZ());

		directionScore -= Math.abs(f[1].pitch() - pf[1].pitch());
		directionScore -= Math.abs(f[1].roll() - pf[1].roll());
		directionScore -= Math.abs(f[1].yaw() - pf[1].yaw());

		score[0] = positionScore;
		score[1] = directionScore;

		return score;
	}
}
