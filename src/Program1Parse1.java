import components.map.Map;
import components.program.Program;
import components.program.Program1;
import components.queue.Queue;
import components.simplereader.SimpleReader;
import components.simplereader.SimpleReader1L;
import components.simplewriter.SimpleWriter;
import components.simplewriter.SimpleWriter1L;
import components.statement.Statement;
import components.utilities.Reporter;
import components.utilities.Tokenizer;

/**
 * Layered implementation of secondary method {@code parse} for {@code Program}.
 *
 * @author Matthew Cramblett
 * @author Cameron Long
 *
 */
public final class Program1Parse1 extends Program1 {

    /*
     * Private members --------------------------------------------------------
     */

    /**
     * Parses a single BL instruction from {@code tokens} returning the
     * instruction name as the value of the function and the body of the
     * instruction in {@code body}.
     *
     * @param tokens
     *            the input tokens
     * @param body
     *            the instruction body
     * @return the instruction name
     * @replaces body
     * @updates tokens
     * @requires [<"INSTRUCTION"> is a proper prefix of tokens]
     * @ensures <pre>
     * if [an instruction string is a proper prefix of #tokens] then
     *  parseInstruction = [name of instruction at start of #tokens]  and
     *  body = [Statement corresponding to statement string of body of
     *          instruction at start of #tokens]  and
     *  #tokens = [instruction string at start of #tokens] * tokens
     * else
     *  [reports an appropriate error message to the console and terminates client]
     * </pre>
     */
    private static String parseInstruction(Queue<String> tokens, Statement body) {
        assert tokens != null : "Violation of: tokens is not null";
        assert body != null : "Violation of: body is not null";
        assert tokens.length() > 0 && tokens.front().equals("INSTRUCTION") : ""
        + "Violation of: <\"INSTRUCTION\"> is proper prefix of tokens";
        tokens.dequeue(); //remove "INSTRUCTION"
		//check if instruction name is same as a primitive name:
        Reporter.assertElseFatalError(!tokens.front().equals("move"), "Instruction name same as primitive: " + tokens.front());
        Reporter.assertElseFatalError(!tokens.front().equals("turnleft"), "Instruction name same as primitive: " + tokens.front());
        Reporter.assertElseFatalError(!tokens.front().equals("turnright"), "Instruction name same as primitive: " + tokens.front());
        Reporter.assertElseFatalError(!tokens.front().equals("infect"), "Instruction name same as primitive: " + tokens.front());
        Reporter.assertElseFatalError(!tokens.front().equals("skip"), "Instruction name same as primitive: " + tokens.front());
        //check if instruction name is a valid identifier (or a keyword or condition):
        Reporter.assertElseFatalError(Tokenizer.isIdentifier(tokens.front()), "Instruction name is not a valid identifier: " + tokens.front());
        
        String name = tokens.dequeue();//store name
        tokens.dequeue(); //remove "IS"
        Queue<String> instrTokens = tokens.newInstance();
        while(!tokens.front().equals(name)){
        	instrTokens.enqueue(tokens.dequeue()); //store each token from the instruction
        }
        instrTokens.flip(); //flip to access last element
        instrTokens.dequeue(); //remove "END" that was stored
        instrTokens.flip(); //flip to set back
        
        String endingName = tokens.dequeue(); //store closing name and check that it's equal to actual name:
        Reporter.assertElseFatalError(endingName.equals(name), "Instruction name does not match: " + name);
        instrTokens.enqueue(Tokenizer.END_OF_INPUT); //required by parseBlock
 
        body.parseBlock(instrTokens); //parse the instruction

        return name;
    }

    /*
     * Constructors -----------------------------------------------------------
     */

    /**
     * No-argument constructor.
     */
    public Program1Parse1() {
        super();
    }

    /*
     * Public methods ---------------------------------------------------------
     */

    @Override
    public void parse(SimpleReader in) {
        assert in != null : "Violation of: in is not null";
        assert in.isOpen() : "Violation of: in.is_open";
        Queue<String> tokens = Tokenizer.tokens(in);
        this.parse(tokens);
    }

    @Override
    public void parse(Queue<String> tokens) {
        assert tokens != null : "Violation of: tokens is not null";
        assert tokens.length() > 0 : ""
        + "Violation of: Tokenizer.END_OF_INPUT is a suffix of tokens";
        
        Statement body = this.newBody();
        Map<String, Statement> context = this.newContext();
        Reporter.assertElseFatalError(tokens.front().equals("PROGRAM"), "ERROR: \"PROGRAM\" expected at beginning.");
        tokens.dequeue(); //remove "PROGRAM"
        
        //check if program name is a valid identifier (or a keyword or condition):
        Reporter.assertElseFatalError(Tokenizer.isIdentifier(tokens.front()), "Program name is not a valid identifier");
        
        String programName = tokens.dequeue(); //store the name
        Reporter.assertElseFatalError(tokens.front().equals("IS"), "ERROR: \"IS\" expected after program name.");
        tokens.dequeue(); //remove "IS"
        
        //parse all instructions:
        while(tokens.front().equals("INSTRUCTION")){
    		Statement instrBody = this.newBody(); //create body for instruction
    		String instrName = parseInstruction(tokens, instrBody); //parse the instruction, store the name
    		Reporter.assertElseFatalError(!context.hasKey(instrName), "Non-unique instruction name: " + tokens.front());
    		context.add(instrName, instrBody); //add instruction to context
        }
        
        Reporter.assertElseFatalError(tokens.front().equals("BEGIN"), "ERROR: \"BEGIN\" expected.");
        tokens.dequeue(); //remove "BEGIN"  
        
        tokens.flip(); //flip to access last element
        Reporter.assertElseFatalError(tokens.front().equals(Tokenizer.END_OF_INPUT), "ERROR: extra text after program end");
        tokens.dequeue(); //remove END_OF_INPUT
        Reporter.assertElseFatalError(tokens.front().equals(programName), "ERROR: Program name does not match at beginning and end");
        tokens.dequeue(); //remove ending name
        Reporter.assertElseFatalError(tokens.front().equals("END"), "ERROR: Program does not have and \"END\" statement.");
        tokens.dequeue(); //remove the last "END" 
        tokens.flip(); //flip to restore order
        

        
        tokens.enqueue(Tokenizer.END_OF_INPUT);
        //parse the main body of the program:
        body.parseBlock(tokens); 
        
        //assemble program:
        this.replaceName(programName);
        this.replaceBody(body);
        this.replaceContext(context);
    }

    /*
     * Main test method -------------------------------------------------------
     */

    /**
     * Main method.
     *
     * @param args
     *            the command line arguments
     */
    public static void main(String[] args) {
        SimpleReader in = new SimpleReader1L();
        SimpleWriter out = new SimpleWriter1L();
        /*
         * Get input file name
         */
        out.print("Enter valid BL program file name: ");
        String fileName = in.nextLine();
        /*
         * Parse input file
         */
        out.println("*** Parsing input file ***");
        Program p = new Program1Parse1();
        SimpleReader file = new SimpleReader1L(fileName);
        Queue<String> tokens = Tokenizer.tokens(file);
        file.close();
        p.parse(tokens);
        /*
         * Pretty print the program
         */
        out.println("*** Pretty print of parsed program ***");
        p.prettyPrint(out);

        in.close();
        out.close();
    }

}
