package br.unb.cic.iris.core.model;

public abstract class FolderContent {
	public FolderContent() {
		this(null);
	}

	public FolderContent(String id) {
		super();
		this.id = id;
	}

	private String id;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
}