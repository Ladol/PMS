package pt.ulisboa.tecnico.rnl.dei.dms.exceptions;

public enum ErrorMessage {
    INVALID_JURY_SIZE("Número inválido de membros do júri: %s", 999),
    INVALID_USER_TYPE("Ação permitida apenas para estudantes", 1000),
	NO_SUCH_PERSON("Não existe nenhuma pessoa com o ID %s", 1001),
	PERSON_NAME_NOT_VALID("O nome da pessoa especificado não é válido.", 1002),
	PERSON_ALREADY_EXISTS("Já existe uma pessoa com o ID %s", 1003);

	private final String label;
	private final int code;

	ErrorMessage(String label, int code) {
		this.label = label;
		this.code = code;
	}

	public String getLabel() {
		return this.label;
	}

	public int getCode() {
		return this.code;
	}
}
