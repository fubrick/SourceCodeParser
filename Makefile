#
# A simple makefile for 
# 1) compiling a java program
# 2) running a series of tests 
# 3) cleaning the working directory of any class files from previous test.
#

# define a makefile variable for the java compiler
#
JCC = javac

# define run variable
#
JAV = java

# typing 'make' will invoke the first target entry in the makefile 
# (the default one in this case)
#
default: compileFiles


# this target entry builds the class/es for the CCppJavaParser.java file
# the Average.class file is dependent on the Average.java file
# and the rule associated with this entry gives the command to create it
#
compileFiles: CCppJavaParser.java
	$(JCC) *.java

# the run comands will run each test and append the results to a file
# named stderr.  If the CCppJavaParser finishes with a (-1) then Error 255 should
# display on the console as well.  If a file is succeful and the java program
# returns a (0) then nothing will be displayed on console. 
run:
	echo "\n\nTest run 1:" >> stderr
	$(JAV) CCppJavaParser source_code_file.txt >> stderr
run2:
	echo "\n\nTest run 2:" >> stderr
	$(JAV) CCppJavaParser main.cpp >> stderr
run3:
	echo "\n\nTest run 3:" >> stderr
	$(JAV) CCppJavaParser test.java >> stderr

# To start over from scratch, type 'make clean'.  
# Removes all .class files, so that the next make rebuilds them
#
clean: 
	$(RM) *.class
	$(RM) stderr
