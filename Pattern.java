import java.util.ArrayList;
import com.leapmotion.leap.*;

public class Pattern {

	ArrayList<Vector[][]> fingerData;
	ArrayList<Vector[]> palmData;
	int[] fingerKey;
	int length; //number of stored frames
	boolean rightHanded;

	final double MOVEMENT_THRESHOLD = 30;

	public Pattern(boolean rightHanded) {
		fingerData = new ArrayList<Vector[][]>();
		palmData = new ArrayList<Vector[]>();
		fingerKey = new int[5];
		length = 0;
		this.rightHanded = rightHanded;
	}

	public void endRecording() {
		for (int i=0; i<CustomListener.RECORDING_TIMEOUT; i++) {
			fingerData.remove(fingerData.size()-1);
			palmData.remove(palmData.size()-1);
			length--;
		}

		normalize();
	}

	public double compare(Pattern p) {

		//generate dynamic time warp matrix
		double[][] dtwMatrix = new double[length][p.length];

		for (int i=0; i<length; i++) {
			for (int j=0; j<p.length; j++) {
				double[] scores = new double[3];

				double cost = compareFrames(i, p, j)[0];
				
				if (i > 0 && j > 0)
					scores[0] = cost + dtwMatrix[i-1][j-1];
				else
					scores[0] = -1;

				if (i > 0)
					scores[1] = cost + dtwMatrix[i-1][j];
				else
					scores[1] = -1;

				if (j > 0)
					scores[2] = cost + dtwMatrix[i][j-1];
				else
					scores[2] = -1;

				double min = dtwMin(scores);

				if (min == -1) //must be top left cell
					dtwMatrix[i][j] = cost;
				else
					dtwMatrix[i][j] = min;
			}
		}

		return dtwMatrix[length - 1][p.length - 1];
	}

	public double dtwMin(double[] scores) {
		double min = -1;

		for (double s : scores)
			if (s != -1 && (min == -1 || s < min))
				min = s;

		return min;
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

			//read hand other direction
			if (!rightHanded) {
				Vector[][] fings = new Vector[5][2];
				for (int i=0; i<vFingers.length; i++)
					fings[4-i] = vFingers[i];
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
					if (rightHanded) {
						fingerPos.setX(fingerPos.getX() - initPos.getX());
						fingerPos.setY(fingerPos.getY() - initPos.getY());
						fingerPos.setZ(fingerPos.getZ() - initPos.getZ());
					} else {
						fingerPos.setX(-(fingerPos.getX() - initPos.getX()));
						fingerPos.setY(-(fingerPos.getY() - initPos.getY()));
						fingerPos.setZ(-(fingerPos.getZ() - initPos.getZ()));
					}
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
		return compareFrames(fingerData.get(frameNum), p.fingerData.get(frameNum),
							 palmData.get(frameNum), p.palmData.get(frameNum), false);
	}

	public double[] compareFrames(int fn1, Pattern p, int fn2) {
		return compareFrames(fingerData.get(fn1), p.fingerData.get(fn2),
							 palmData.get(fn1), p.palmData.get(fn2), false);
	}

	public double[] compareFrames(int fn1, int fn2) {
		return compareFrames(fingerData.get(fn1), fingerData.get(fn2),
							 palmData.get(fn1), palmData.get(fn2), true);
	}

	public double[] compareFrames(Vector[][] fingerFrame1, Vector[][] fingerFrame2,
								  Vector[] palmFrame1, Vector[] palmFrame2, boolean isMovementTest) {
		
		double[] score = new double[2];

		double positionScore = 0;
		double directionScore = 0;

		for (int i=0; i<5; i++) {
			Vector[] finger1 = fingerFrame1[i];
			Vector[] finger2 = fingerFrame2[i];

			Vector fp1 = finger1[0]; //position
			Vector fp2 = finger2[0];
			Vector fd1 = finger1[1]; //direction
			Vector fd2 = finger2[1];

			if (fp1 != null && fp2 != null) {
				positionScore += Math.abs(fp1.getX() - fp2.getX());
				positionScore += Math.abs(fp1.getY() - fp2.getY());
				positionScore += Math.abs(fp1.getZ() - fp2.getZ());

				directionScore += Math.abs(fd1.pitch() - fd2.pitch());
				directionScore += Math.abs(fd1.roll() - fd2.roll());
				directionScore += Math.abs(fd1.yaw() - fd2.yaw());
			} else if (!isMovementTest) {
				positionScore += 100;
			}
		}

		Vector pp1 = palmFrame1[0]; //position
		Vector pp2 = palmFrame2[0];
		Vector pd1 = palmFrame1[1]; //direction
		Vector pd2 = palmFrame2[1];

		positionScore += Math.abs(pp1.getX() - pp2.getX());
		positionScore += Math.abs(pp1.getY() - pp2.getY());
		positionScore += Math.abs(pp1.getZ() - pp2.getZ());

		directionScore += Math.abs(pd1.pitch() - pd2.pitch());
		directionScore += Math.abs(pd1.roll() - pd2.roll());
		directionScore += Math.abs(pd1.yaw() - pd2.yaw());

		score[0] = positionScore;
		score[1] = directionScore;

		return score;
	}
}
