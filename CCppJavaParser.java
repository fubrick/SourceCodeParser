import java.io.*;
import java.util.*;

/**
 * Program to parse and find errors in a provided source code
 * Balance is checked and if any erros are found, the result will print
 * each line were an error occurs.
 * @author Jeremy Jones
 * @return -1 if errors found or 0 if source file is free of errors.
 */
public class CCppJavaParser
{
	public static Scanner in = new Scanner(System.in);
	public static void main(String[]args)
	{
		int answer = 0;
		String sourceCode = args[0];
		CodeScanner reader = new CodeScanner();
		LineAnalyzer tester = new LineAnalyzer();

		reader.openFile(sourceCode);
		reader.readFile(tester);
		reader.closeFile();

		tester.clearStacks();
		if(!tester.hasNoErrors())
		{
			System.out.println("\nErrors Found.");
			tester.printErrorMessages();
			System.exit(-1);
		}
		else
		{
			System.out.println("\nSuccess");
			System.exit(0);
		}
	}
}

/**
 * Class that will scan in the given dataFile and 
 * send each line to LineAnalyzer class to be parsed
 */
class CodeScanner
{
	private Scanner x;

	/**
	 * method to open a new file
	 * @param dataFile String name of the file to be scanned and parsed
	 */
	public void openFile(String dataFile)
	{
		try
		{
			x = new Scanner(new File(dataFile));
		}
		catch(Exception e)
		{
			System.out.println("Could not find file.");
		}
	}

	/**
	 * method to read a file line by line and send each
	 * line to the Line Analyzer for parsing
	 * @param tester LineAnalyzer object that will parse file
	 */
	public void readFile(LineAnalyzer tester)
	{
		while(x.hasNextLine())
		{
			String initialLine = x.nextLine();
			tester.addSourceCode(initialLine);
			if(initialLine.contains("/*") || initialLine.contains("*/"))
			{
				String lineNotComment = tester.commentParser(initialLine);
				String lineIgnoringQuotes = tester.quoteParser(lineNotComment);
				tester.isBalanced(lineIgnoringQuotes);

				if(lineIgnoringQuotes.contains("#"))
				{
					tester.hashParser(lineIgnoringQuotes);
				}
			}
			else if(tester.isComment())
			{
				String lineIgnoringQuotes = tester.quoteParser(initialLine);
				tester.isBalanced(lineIgnoringQuotes);


				if(lineIgnoringQuotes.contains("#"))
				{
					tester.hashParser(lineIgnoringQuotes);
				}
			}
			tester.lineIncrease();
		}
	}

	/**
	 * Method to close file
	 */
	public void closeFile()
	{	x.close();	}
}

/**
 * class to analyze each line of code and correctly
 * find any mistakes
 */
class LineAnalyzer
{
	/**
	 * Method to indicate if the comment stack is empty
	 * @return boolean returns true if empty
	 */
	public boolean isComment()
	{	return comment.isEmpty();	}

	/**
	 * Method to indicate if the Quote stack is empty
	 * @return boolean returns true if empty
	 */
	public boolean isQuote()
	{	return doubleQuote.isEmpty();	}

	/**
	 * Method to indicate if there are any errors
	 * @return boolean returns true if no errors have been found
	 */
	public boolean hasNoErrors()
	{	return errorMessages.isEmpty();	}

	/**
	 * Method to increase the line count
	 */
	public void lineIncrease()
	{	lineCount++;	}

	/**
	 * Method to add a line of code to an array for reference
	 * @param expression String line of code
	 */
	public void addSourceCode(String expression)
	{	sourceCode.add(expression);	}

	/**
	 * Method to clear out the stacks at the end of parsing
	 * entire source code.  Any remaining errors are found.
	 */
	public void clearStacks()
	{
		while(bracket.isEmpty() == false)
		{
			errorMessages.add("Line-" + --lineCount + ": Error: Bracket: " + bracket.pop() + " reached end of file while parsing.");
		}
		while(comment.isEmpty() == false)
			errorMessages.add(comment.pop());
		while(hashIf.isEmpty() == false)
			errorMessages.add(hashIf.pop());
	}

	/**
	 * Method to parse preprocessor lines
	 * @param expression String line of code
	 */
	public void hashParser(String expression)
	{
		StringTokenizer st = new StringTokenizer(expression);
		String temp = st.nextToken();
		if(temp.charAt(0) != '#')
		{
			return;
		}
		switch (temp)
		{
			case HASH_IF_N_DEFINED_ALT:
			case HASH_IF_N_DEFINED:
			case HASH_IF:
			case HASH_IFDEF:
			case HASH_IFNDEF:
				String error = "Line " + lineCount + ":\t\t\"" + sourceCode.get(lineCount - 1) + "\" Error: Unterminated conditional directive";
				hashIf.push(error);
				break;
			case HASH_ELSE:
				if(hashElse.isEmpty())
					hashElse.push(temp);
				else
					errorMessages.add("Line " + lineCount + ":\t\t\"" + sourceCode.get(lineCount - 1) + "\" Error: #else after #else");
				break;
			case HASH_ELIF:
				if (hashIf.isEmpty())
					errorMessages.add("Line " + lineCount + ":\t\t\"" + sourceCode.get(lineCount - 1) + "\" Error: #elif without #if");
				if (!hashElse.isEmpty())
					errorMessages.add("Line " + lineCount + ":\t\t\"" + sourceCode.get(lineCount - 1) + "\" Error: #elif after #else");
				break;
			case HASH_ENDIF:
				if(hashIf.isEmpty())
					errorMessages.add("Line " + lineCount + ":\t\t\"" + sourceCode.get(lineCount - 1) + "\" Error: #endif without #if");
				else
				{
					hashIf.pop();
					if(!hashElse.isEmpty())
						hashElse.pop();
				}
		}	
	}

	/**
	 * Method to parse Comments
	 * @param expression String line of code
	 * @return String line that is not commented out
	 */
	private String singleCommentParser(String expression)
	{
		String answer = "";

		if(expression.contains(BEGIN_COMMENT))
		{
			answer = expression.substring(0, expression.indexOf(BEGIN_COMMENT));
			commentSpot = expression.lastIndexOf(BEGIN_COMMENT);
			String error = "Line " + lineCount + ": char " + commentSpot + ":\t\"" + sourceCode.get(lineCount -1) + "\" : Error: No matching end to Comment.";
			comment.push(error);
		}
		else if(!comment.isEmpty() && expression.contains(END_COMMENT))
		{
			comment.pop();
			answer = expression.substring(expression.indexOf(END_COMMENT) + 2);
		}
		else if(comment.isEmpty() && expression.contains(END_COMMENT))
		{
			answer = expression.substring(expression.indexOf(END_COMMENT) + 2);
			commentSpot = expression.lastIndexOf(END_COMMENT);
			errorMessages.add("Line " + lineCount + ": char " + commentSpot + ":\t\"" + sourceCode.get(lineCount -1) + "\" : Error: No matching start to Comment.");
		}
		return answer;
	}

	/**
	 * Method to parse Comments
	 * @param expression String line of code
	 * @return String line that is not commented out
	 */
	public String commentParser(String expression)
	{
		String answer = "";
		if(expression.contains(BEGIN_COMMENT) && expression.contains(END_COMMENT))
		{
			int snip1 = expression.indexOf(BEGIN_COMMENT);
			int snip2 = expression.indexOf(END_COMMENT) + 2;
			if (snip1 < snip2)
				return expression.substring(0, snip1).concat(expression.substring(snip2));
			else if (comment.isEmpty())
			{
				System.out.println("line: " + lineCount);
				singleCommentParser(expression.substring(0, snip1));
				singleCommentParser(expression.substring(snip1));
				return expression.substring(0, snip1);
			}
			else
				return expression.substring(snip2, snip1);
		}
		else
			return singleCommentParser(expression);
	}

	/**
	 * Method to parse Quotes
	 * @param expression String line of code to be parsed
	 * @return String line that is must still be checked 
	 */
	public String quoteParser(String expression)
	{
		String answer = "";
		if (expression.contains("\""))
		{
			String temp = doubleQuoteParser(expression);
			if (temp.contains("\'"))
				singleQuoteParser(expression);
			return temp;
		}
		else
			return singleQuoteParser(expression);
	}

	//See quoteParser javadoc
	private String doubleQuoteParser(String expression)
	{
		boolean ignore = false;
		String answer = "";

		for(int i = 0; i < expression.length(); i++)
		{

			switch(expression.charAt(i))
			{
				case DOUBLE_QUOTE:
					if(doubleQuote.isEmpty())
					{
						doubleQuote.push(expression.charAt(i));
						ignore = true;
					}
					else if(!doubleQuote.isEmpty() && !quote.isEmpty())
						doubleQuote.pop();
					else
					{
						doubleQuote.pop();
						ignore = false;
					}
					break;
				default:
					if(ignore == false)
						answer += expression.charAt(i);
					break;
			}
		}
		while(!doubleQuote.isEmpty())
		{
			int spot2 = expression.lastIndexOf(DOUBLE_QUOTE);
			errorMessages.add("Line " + lineCount + ": char " + spot2 + ":\t\"" + sourceCode.get(lineCount -1) + "\" Error: unclosed string literal: " + doubleQuote.pop());
		}
		return answer;
	}

	//See quoteParser javadoc
	private String singleQuoteParser(String expression)
	{
		boolean ignore = false;
		String answer = "";

		for(int i = 0; i < expression.length(); i++)
		{
			switch (expression.charAt(i))
			{
				case SINGLE_QUOTE:
					if(quote.isEmpty())
					{
						quote.push(expression.charAt(i));
						ignore = true;
					}
					else if (!quote.isEmpty() && !doubleQuote.isEmpty())
						quote.pop();
					else
					{
						quote.pop();
						ignore = false;
					}
				default:
					if(ignore == false)
						answer += expression.charAt(i);
					break;
			}
		}
		while(!quote.isEmpty())
		{
			int spot = expression.lastIndexOf(SINGLE_QUOTE);
			errorMessages.add("Line " + lineCount + ": char " + spot + ":\t\"" + sourceCode.get(lineCount -1) + "\" Error: unclosed string literal: " + quote.pop());
		}
		return answer;
	}

	/**
	 * method to parse brackets
	 * @param expression String line of code to be parsed
	 */
	public void isBalanced(String expression)
	{

		for(int i = 0; i < expression.length(); i++)
		{
			switch(expression.charAt(i))
			{
				case LEFT_NORMAL:
				case LEFT_SQUARE:
				case LEFT_CURLY:
					bracket.push(expression.charAt(i));
					break;
				case RIGHT_NORMAL:
					if(bracket.isEmpty() || (bracket.peek() != LEFT_NORMAL))
						errorMessages.add("Line " + lineCount + ": char " + i + ":\t\"" + sourceCode.get(lineCount -1 ) + "\" Error: Unmatched '" + RIGHT_NORMAL + "'");
					if(!bracket.isEmpty() && bracket.peek() == LEFT_NORMAL)
						bracket.pop();
					break;
				case RIGHT_SQUARE:
					if(bracket.isEmpty() || (bracket.peek() != LEFT_SQUARE))
						errorMessages.add("Line " + lineCount + ": char " + i + ":\t\"" + sourceCode.get(lineCount -1 ) + "\" Error: Unmatched '" + RIGHT_SQUARE + "'");
					if(!bracket.isEmpty() && bracket.peek() == LEFT_SQUARE)
						bracket.pop();
					break;
				case RIGHT_CURLY:
					if(bracket.isEmpty() || (bracket.peek() != LEFT_CURLY))
						errorMessages.add("Line " + lineCount + ": char " + i + ":\t\"" + sourceCode.get(lineCount - 1) + "\" Error: Unmatched '" + RIGHT_CURLY + "'");
					if(!bracket.isEmpty() && bracket.peek() == LEFT_CURLY)
						bracket.pop();
					break;
			}
		}
	}

	/**
	 * Method to print error messages when program is finished parsing
	 */
	public void printErrorMessages()
	{
		ListIterator<String> lister = errorMessages.listIterator();
		while(lister.hasNext())
		{
			System.out.println(lister.next());
		}
	}

	private int lineCount = 1;
	private int commentSpot;
	private Stack<String> comment = new Stack<>();
	private Stack<Character> bracket = new Stack<>();
	private Stack<Character> quote = new Stack<>();
	private Stack<Character> doubleQuote = new Stack<>();
	private Stack<String> hashIf = new Stack<>();
	private Stack<String> hashElse = new Stack<>();
	private ArrayList<String> sourceCode = new ArrayList<>();
	private ArrayList<String> errorMessages = new ArrayList<>();
	private final String BEGIN_COMMENT = "/*";
	private final String END_COMMENT = "*/";
	private final char SINGLE_QUOTE = '\'';
	private final char DOUBLE_QUOTE = '\"';
	private final char LEFT_NORMAL = '(';
	private final char RIGHT_NORMAL = ')';
	private final char LEFT_CURLY = '{';
	private final char RIGHT_CURLY = '}';
	private final char LEFT_SQUARE = '[';
	private final char RIGHT_SQUARE = ']';
	private final String HASH_IFDEF = "#ifdef";
	private final String HASH_IFNDEF = "#ifndef";
	private final String HASH_IF = "#if";
	private final String HASH_IF_N_DEFINED_ALT = "#if!";
	private final String HASH_IF_N_DEFINED = "#if!defined";
	private final String HASH_ELSE = "#else";
	private final String HASH_ELIF = "#elif";
	private final String HASH_ENDIF = "#endif";
}

