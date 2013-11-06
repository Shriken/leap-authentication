build:
	javac -classpath /home/zane/hax/leap-authentication/sdk/LeapJava.jar *.java

run:
	java -classpath ".:/home/zane/hax/leap-authentication/sdk/LeapJava.jar" -Djava.library.path="/home/zane/hax/leap-authentication/sdk/libLeapJava.so" Recorder

clean:
	rm *.class
