package com.universe.nas;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
/*
NAS Is a scripting language it executes directly from command line
 */
public class NAS {

    static boolean hasError = false;

    public static void main(String[] args) throws IOException {
        if(args.length > 1){
            System.out.println("Usage: NAS [script]");
            System.exit(64);
        }else if(args.length == 1){
            runFile(args[0]);
        }else{
            runPrompt();
        }
    }
    // If you start NAS from the command line and give it a path to a file, it reads the file and executes it.
    private static void runFile(String path)throws IOException{
        byte[] bytes = Files.readAllBytes(Paths.get(path));

        run(new String(bytes, Charset.defaultCharset()));
        // Indicate an error in the exit code
        if(hasError) {
            System.exit(65);
        }
    }

    /**
     * If you want a more intimate conversation with you interpreter
     * you can also run it interactively. Fire up NAS without any arguments, and it drops
     * you into a prompt where you can enter and execute code one line at a time
     */
    private static void runPrompt() throws IOException{
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);

        for(;;){
            System.out.println("> ");
            String line = reader.readLine();

            if(line == null)
                break;
            run(line);
            // If the user makes a mistake, it shouldn't kill their entire session
            hasError = false;
        }
    }
    private static void run(String source){
        Scanner scanner = new Scanner(source);

        List<Token> tokens = scanner.scanTokens();

        // just print the tokens
        for(Token token: tokens){
            System.out.println(token);
        }
    }
    // Error Handling
    // Helper tells the user some syntax error
    static void error(int line, String message){
        report(line, "", message);
    }
    // Print the line error in the code
    private static void report(int line, String where, String message){
        System.err.println("[line " + line + "] Error" + where + ": " + message);
        hasError = true;
    }
}
