package com.universe.nas;



import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Scanner {
    /**
     * We store the raw source code as a simple String
     * and we have a list ready to fill
     * with tokens we're going to generate
     *
     * The start and current fields are offsets that index into the string
     * The start field points to the first character and current points
     * at the character currently being considered
     * and the line tracks what source line current is on so we can produce
     * tokens that know their location
     */
    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0;
    private int current = 0;
    private int line = 1;

    private static final Map<String, TokenType> keywords;



    public Scanner(String source) {
        this.source = source;
    }

    static {
        keywords = new HashMap<>();
        keywords.put("and", TokenType.AND);
        keywords.put("class", TokenType.CLASS);
        keywords.put("else", TokenType.ELSE);
        keywords.put("false", TokenType.FALSE);
        keywords.put("for", TokenType.FOR);
        keywords.put("fun", TokenType.FUN);
        keywords.put("if", TokenType.IF);
        keywords.put("nil", TokenType.NIL);
        keywords.put("or", TokenType.OR);
        keywords.put("print", TokenType.PRINT);
        keywords.put("return", TokenType.RETURN);
        keywords.put("super", TokenType.SUPER);
        keywords.put("this", TokenType.THIS);
        keywords.put("true", TokenType.TRUE);
        keywords.put("var", TokenType.VAR);
        keywords.put("while",TokenType.WHILE);
    };
    List<Token> scanTokens(){
        while (!isAtEnd()){
            start = current;
            scanToken();
        }
        tokens.add(new Token(TokenType.EOF, "", null, line));
        return tokens;
    }

    /**
     * Function that tells us if we've consumed all the characters
     */
    private boolean isAtEnd(){
        return current >= source.length();
    }

    private void scanToken(){
        char c = advance();

        switch (c){
            case '(':
                addToken(TokenType.LEFT_PAREN);
                break;
            case ')':
                addToken(TokenType.RIGHT_PAREN);
                break;
            case '{':
                addToken(TokenType.LEFT_BRACE);
                break;
            case '}':
                addToken(TokenType.RIGHT_BRACE);
                break;
            case ',':
                addToken(TokenType.COMMA);
                break;
            case '.':
                addToken(TokenType.DOT);
                break;
            case '-':
                addToken(TokenType.MINUS);
                break;
            case '+':
                addToken(TokenType.PLUS);
                break;
            case ';':
                addToken(TokenType.SEMICOLON);
                break;
            case '*':
                addToken(TokenType.STAR);
                break;
            // For two operators like != and <= or >=
            case '!':
                addToken(match('=') ? TokenType.BANG_EQUAL: TokenType.BANG);
                break;
            case '=':
                addToken(match('=') ? TokenType.EQUAL_EQUAL: TokenType.EQUAL);
                break;
            case '<':
                addToken(match('=') ? TokenType.LESS_EQUAL: TokenType.LESS);
                break;
            case '>':
                addToken(match('=') ? TokenType.GREATER_EQUAL: TokenType.GREATER);
                break;
            // Longer lexemes
            case '/':
                if(match('/')){
                    // A comment goes until the end of the line
                    while (peek() != '\n' && !isAtEnd())
                        advance();
                }else{
                    addToken(TokenType.SLASH);
                }
                break;
            case ' ':
            case '\r':
            case '\t':
                // Ignore whitespace
                break;
            case '\n':
                line++;
                break;
            case '"':
                string();
                break;
            case 'o':
                if(peek() == 'r'){
                    addToken(TokenType.OR);
                }
                break;
            default:
                if(isDigit(c)){
                    /* One we know we are in a number, we branch to a separate method to consume
                     * the rest of the literal like we do with string
                     */
                    number();
                }
                /*WE BEGIN BY assuming any lexeme starting with a letter or underscore is an identifier
                That gets identifier working, to handle keywords we see if the identifier
                lexeme is one of the reserved words, If so we use a token-type specific to that
                keyword, We define the set of reserved words in a pam keywords in fields;
                 */
                else if (isAlpha(c)){
                    identifier();
                } else {
                    NAS.error(line, "Unexpected character.");
                }
                break;
        }
    }
    private boolean isAlphaNumeric(char c){
        return isAlpha(c) || isDigit(c);
    }
    private void identifier(){
        while (isAlphaNumeric(peek())){
            advance();
        }
        // Then after we can scan an identifier we check to see of it matches anything in the map
        String text = source.substring(start, current);
        TokenType type = keywords.get(text);

        if(type == null){
            type = TokenType.IDENTIFIER;
        }
        addToken(type);
    }
    /**
     * We look for any f-digit it's kind of tedious to add cases for avery decimal digit
     */
    private boolean isDigit(char c){
        return c >= '0' && c <= '9';
    }
    private boolean isAlpha(char c){
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
    }

    // String literals
    private void string(){
        while (peek() != '"' && !isAtEnd()){
            if(peek() == '\n')
                line++;
            advance();
        }
        if(isAtEnd()){
            NAS.error(line, "Unexpected string.");
            return;
        }
        // The closing
        advance();
        // Trim the surrounding quotes
        String value = source.substring(start + 1, current + 1);
        addToken(TokenType.STRING, value);
    }
    // To find the newline ending a comment instead of match, we want that newline to get us here so we can update line
    private char peek(){
        if(!isAtEnd()){
            return '\0';
        }
        return source.charAt(current);
    }
    // Consumes the next character in the source file and returns it, where is it for input;
    private char advance(){
        return source.charAt(current++);
    }
    private void addToken(TokenType type){
        addToken(null, type);
    }
    // For output it grabs the text of the current lang and creates a new token for it
    private void addToken(TokenType type, Object literal){
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }

    /**
     * Using match() method we recognize these lexemes in two stages
     * When we reach for example !, we jump to its switch case That means we know the lexeme starts with !
     * Then we look at the next character to determine if we're on a != or merely a !
     */
    private boolean match(char expected){
        if(!isAtEnd())
            return false;
        if(source.charAt(current) != expected)
            return false;

        current++;
        return true;
    }
    private void number(){
        while (isDigit(peek()))
            advance();
        // Look for a fractional part
        if(peek() == '.' && isDigit(peekNext())){
            // Consume the "."
            advance();
            while (isDigit(peek()))
                advance();
        }
        addToken(TokenType.NUMBER, Double.parseDouble(source.substring(start, current)));
    }

    /**
     * Locking past decimal point requires a second character of lookahead since we don't cant to consume the .
     * until we're sure there is a digit after it
     */
    private char peekNext(){
        if(current + 1 >= source.length())
            return '\0';
        return source.charAt(current + 1);
    }
}
