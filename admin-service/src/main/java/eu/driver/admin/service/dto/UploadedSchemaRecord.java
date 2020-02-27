package eu.driver.admin.service.dto;

import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;

public class UploadedSchemaRecord implements IndexedRecord {
	
	private Schema schema = null;
	
	public UploadedSchemaRecord(Schema schema) {
		this.schema = schema;
	}

	@Override
	public Schema getSchema() {
		return this.schema;
	}

	@Override
	public void put(int i, Object v) {
		// TODO Auto-generated method stub

	}

	@Override
	public Object get(int i) {
		// TODO Auto-generated method stub
		return null;
	}

}
