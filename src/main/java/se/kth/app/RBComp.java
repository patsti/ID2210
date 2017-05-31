/*
 * 2016 Royal Institute of Technology (KTH)
 *
 * LSelector is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package se.kth.app;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.kth.app.port.GBEBPort;
import se.kth.app.port.RBPort;
import se.kth.app.test.ADD;
import se.kth.app.test.Ping;
import se.kth.app.test.Pong;
import se.kth.app.test.RB;
import se.kth.app.test.REMOVE;
import se.kth.app.test.GBEB;
import se.kth.croupier.util.CroupierHelper;
import se.kth.system.HostMngrComp.Init.SET;
import se.sics.kompics.ClassMatchedHandler;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Negative;
import se.sics.kompics.Positive;
import se.sics.kompics.Start;
import se.sics.kompics.network.Network;
import se.sics.kompics.network.Transport;
import se.sics.ktoolbox.croupier.CroupierPort;
import se.sics.ktoolbox.croupier.event.CroupierSample;
import se.sics.ktoolbox.util.identifiable.Identifier;
import se.sics.ktoolbox.util.network.KAddress;
import se.sics.ktoolbox.util.network.KContentMsg;
import se.sics.ktoolbox.util.network.KHeader;
import se.sics.ktoolbox.util.network.basic.BasicContentMsg;
import se.sics.ktoolbox.util.network.basic.BasicHeader;

public class RBComp extends ComponentDefinition {

  private final Logger LOG = LoggerFactory.getLogger(RBComp.class);
  private String logPrefix = " ";

  //*******************************CONNECTIONS********************************
  Positive<GBEBPort> gbebPort = requires(GBEBPort.class);
  Negative<RBPort> rbPort = provides(RBPort.class);
  //**************************************************************************
  private KAddress selfAdr;
  private List<ADD> delivered;
  private SET set;
  
  public RBComp(Init init) {
    selfAdr = init.selfAdr;
    delivered = new ArrayList();
    set = init.set;
    logPrefix = "<nid:" + selfAdr.getId() + ">";

    //LOG.info("{}initiating...", logPrefix);

    subscribe(handleStart, control);
    subscribe(handleAddGBEB, gbebPort);
    subscribe(handleRemoveGBEB, gbebPort);
    subscribe(handleAddBroadcast, rbPort);
    subscribe(handleRemoveBroadcast, rbPort);
  }

  Handler handleStart = new Handler<Start>() {
    @Override
    public void handle(Start event) {
      //LOG.info("{}starting...", logPrefix);
    }
  };
  
  Handler handleAddBroadcast = new Handler<ADD>() {
	    @Override
	    public void handle(ADD gossip) {
	    	trigger(gossip, gbebPort);
	    }
	  };

  Handler handleRemoveBroadcast = new Handler<REMOVE>() {
	    @Override
	    public void handle(REMOVE gossip) {
	    	trigger(gossip, gbebPort);
	    }
	  };
  
	  
  Handler handleAddGBEB = new Handler<ADD>() {
	  @Override
	  public void handle(ADD msg) {
			  trigger(msg, rbPort);
	  }
  };
  
  Handler handleRemoveGBEB = new Handler<REMOVE>() {
	  @Override
	  public void handle(REMOVE msg) {
		  for(int j=0; j < delivered.size(); j++){
				if(delivered.get(j).getID() == msg.getID()){
					delivered.remove(j);
				  break;
			  }
		  }
		  trigger(msg, rbPort);
	  }
  };
  
  public static class Init extends se.sics.kompics.Init<RBComp> {

	  public final KAddress selfAdr;
	  public final Identifier gradientOId;
	  public final SET set;

	  public Init(KAddress selfAdr, Identifier gradientOId, SET set) {
	      this.selfAdr = selfAdr;
	      this.gradientOId = gradientOId;
	      this.set = set;
	  }
  }
}
