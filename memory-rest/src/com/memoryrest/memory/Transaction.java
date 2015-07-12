package com.memoryrest.memory;

import java.io.Serializable;


public interface Transaction extends Serializable {

	public void executeOn(
			StringBuilder prevalentSystem) throws Exception;

}
