package se.kth.app.port;

import se.kth.app.test.ADD;
import se.kth.app.test.GBEB;
import se.kth.app.test.REMOVE;
import se.sics.kompics.PortType;

public class GBEBPort extends PortType {{
	positive(ADD.class);
	negative(ADD.class);
    request(ADD.class);
    indication(ADD.class);
    
    positive(REMOVE.class);
	negative(REMOVE.class);
    request(REMOVE.class);
    indication(REMOVE.class);
}}
