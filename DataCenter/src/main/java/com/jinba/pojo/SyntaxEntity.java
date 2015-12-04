package com.jinba.pojo;

public class SyntaxEntity {
	
	private String syntax;
	private SyntaxType type;

	public String getSyntax() {
		return syntax;
	}

	public void setSyntax(String syntax) {
		this.syntax = syntax;
	}

	public SyntaxType getType() {
		return type;
	}

	public void setType(SyntaxType type) {
		this.type = type;
	}

	public enum SyntaxType {
		html,
		string,
		json,
		;
	}

}
