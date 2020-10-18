module.exports = {
  // Use Single Quote
  singleQuote: true,
  // Add semicolon at the end of each line
  semi: true,
  // Set default tab width to 2
  tabWidth: 2,
  // Disable tab for indentation
  useTabs: false,
  /*  Have heading and trailing space in an object 
      options - 
        true: { foo: bar }
        false: {foo: bar}
  */
  bracketSpacing: true,
  /*  Arrow parentheses to be avoided in an arrow function
      options - 
        avoid: parentheses can be avoided when it is unnecessary, e.g. x => x
        always: always have parentheses
  */
  arrowParens: 'avoid',
  // Default width to change to a new line
  printWidth: 80,
  /*  Place the ending ">" of a JSX element
      options -
        true: place at the end of the last line
          <button
            className="prettier-class"
            id="prettier-id"
            onClick={this.handleClick}>
            Click Here
          </button> 

        false: place at in a separate line
          <button
            className="prettier-class"
            id="prettier-id"
            onClick={this.handleClick}
          >
            Click Here
          </button>
  */
  jsxBracketSameLine: true,
};
