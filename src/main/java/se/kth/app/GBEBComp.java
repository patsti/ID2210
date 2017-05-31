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
import se.kth.app.test.ADD;
import se.kth.app.test.EDGE;
import se.kth.app.test.HistoryRequest;
import se.kth.app.test.HistoryResponse;
import se.kth.app.test.REMOVE;
import se.kth.app.test.VERTEX;
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

public class GBEBComp extends ComponentDefinition {

  private final Logger LOG = LoggerFactory.getLogger(GBEBComp.class);
  private String logPrefix = " ";

  //*******************************CONNECTIONS********************************
  Positive<Network> networkPort = requires(Network.class);
  Positive<CroupierPort> croupierPort = requires(CroupierPort.class);
  Negative<GBEBPort> gbebPort = provides(GBEBPort.class);
  //**************************************************************************
  private KAddress selfAdr;
  private int id;
  private List<ADD> pastAdd = new ArrayList();
  private List<ADD> VA = new ArrayList();
  private List<ADD> EA = new ArrayList();
  private List<REMOVE> pastRemove = new ArrayList();
  private List<REMOVE> VR = new ArrayList();
  private List<REMOVE> ER = new ArrayList();
  private SET set;
  private boolean hasCreatedMsg = false;

  public GBEBComp(Init init) {
    selfAdr = init.selfAdr;
    set = init.set;
    logPrefix = "<nid:" + selfAdr.getId() + ">";
    id = Integer.valueOf(selfAdr.getId().toString());
    
    //LOG.info("{}initiating Gbeb...", logPrefix);

    subscribe(handleStart, control);
    subscribe(handleCroupierSample, croupierPort);  
    subscribe(handleAddGossip, gbebPort);
    if(!set.equals(SET.GrowOnly)){
    	subscribe(handleRemoveGossip, gbebPort);
    }
    if(set.equals(SET.Graph)){
    	subscribe(handleHistoryRequestGraph, networkPort);
    	subscribe(handleHistoryResponseGraph, networkPort);;
    }else{
    	subscribe(handleHistoryRequest, networkPort);
    	subscribe(handleHistoryResponse, networkPort);
    }
  }

  Handler handleStart = new Handler<Start>() {
	  
    @Override
    public void handle(Start event) {
      //LOG.info("{}starting GbebComp...", logPrefix);
    }
  };

  Handler handleAddGossip = new Handler<ADD>() {

    @Override
    public void handle(ADD content) {
    	if(set.equals(SET.Graph)){
    		if(id<10){
    			VA.add(content);
    		}else{
    			EA.add(content);
    		}
    	}else{
    		pastAdd.add(content);
    	}
  	  
    }
  };

  Handler handleRemoveGossip = new Handler<REMOVE>() {

	    @Override
	    public void handle(REMOVE content) {
	    	
	    	for(int i=0; i < pastRemove.size(); i++){
				if(pastRemove.get(i).getID() == content.getID()){
					if(set.equals(SET.OR)){
						if(pastRemove.get(i).getOR_Counter() >= content.getOR_Counter()){
							return;
						}
					}else if (set.equals(SET.Graph)){
						if(pastRemove.get(i).getOR_Counter() == content.getOR_Counter()){
							return;
						}
					}else{
						return;
					}
				}
			}
	    	pastRemove.add(content);
	    }
	  };
  
  Handler handleCroupierSample = new Handler<CroupierSample>() {
    @Override
    public void handle(CroupierSample croupierSample) {
      if (croupierSample.publicSample.isEmpty()) {
    	  return;
      }
      	
      List<KAddress> sample = CroupierHelper.getSample(croupierSample);
      for (KAddress peer : sample) {
		KHeader header = new BasicHeader(selfAdr, peer, Transport.UDP);
		KContentMsg msg = new BasicContentMsg(header, new HistoryRequest());
		trigger(msg, networkPort);
      }
      int id = Integer.valueOf(selfAdr.getId().toString());
    }
  };

  ClassMatchedHandler handleHistoryRequest
  = new ClassMatchedHandler<HistoryRequest, KContentMsg<?, ?, HistoryRequest>>() {

    @Override
    public void handle(HistoryRequest content, KContentMsg<?, ?, HistoryRequest> container) {
      trigger(container.answer(new HistoryResponse(Integer.valueOf(selfAdr.getId().toString()), pastAdd, pastRemove)), networkPort);
    }
  };

  ClassMatchedHandler handleHistoryResponse
  = new ClassMatchedHandler<HistoryResponse, KContentMsg<?, ?, HistoryResponse>>() {

    @Override
    public void handle(HistoryResponse content, KContentMsg<?, ?, HistoryResponse> container) {

    	List<ADD> peerAddHistory = content.getAddHistory();    
    	boolean first = true;

    	for(int i =0; i < peerAddHistory.size(); i++){
    		ADD gossip = peerAddHistory.get(i);
    		if(! addExistsIn(gossip.getID(), pastAdd, gossip.getOR_Counter())){
    			//Check if gossip has been removed
    			if(! removeExistsIn(gossip.getID(), pastRemove, gossip.getOR_Counter())){
    				if(first){
    					LOG.info("{} Updating Add History with <nid:{}>", logPrefix, content.getSrcID());
    					//printGossipList(peerAddHistory, null, "<nid:"+content.getSrcID()+">");
    					//printGossipList(pastAdd, pastRemove, logPrefix);
        				first = false;
        			}
    				pastAdd.add(gossip);
    				
    				trigger(gossip, gbebPort);
    			}    			
    		}
    	}
    	
    	List<REMOVE> peerRemoveHistory = content.getRemoveHistory();    
    	for(int i =0; i < peerRemoveHistory.size(); i++){
    		REMOVE g = peerRemoveHistory.get(i);
    		if(! removeExistsIn(g.getID(), pastRemove, g.getOR_Counter())){
    			boolean done = false;
    			for(ADD a : pastAdd){
    				if(a.getID() == g.getID() && a.getOR_Counter() > g.getOR_Counter()){
    					done = true;
    				}
    			}
    			if(done){
    				pastRemove.add(g);
    				continue;
    			}
    			pastRemove.add(g);
    			for(int j=0; j < pastAdd.size(); j++){
    				if(pastAdd.get(j).getID() == g.getID()){
  					  pastAdd.remove(j);
  					  break;
  				  }
    			}
    			trigger(g, gbebPort);
    		}
    	}
    }
  };
  
  Handler handleAddGossipGraph = new Handler<ADD>() {

	    @Override
	    public void handle(ADD content) {
	    	if(id < 10){
	    		VA.add(content);
	    	}else{
	    		EA.add(content);
	    	}
	  	  
	    }
	  };

	  Handler handleRemoveGossipGraph = new Handler<REMOVE>() {

		    @Override
		    public void handle(REMOVE content) {
		    	if(id<10){
			    	for(int i=0; i < VR.size(); i++){
						if(VR.get(i).getID() == content.getID()){
							if(set.equals(SET.OR)){
								if(VR.get(i).getOR_Counter() >= content.getOR_Counter()){
									return;
								}
							}else if (set.equals(SET.Graph)){
								if(VR.get(i).getOR_Counter() == content.getOR_Counter()){
									return;
								}
							}else{
								return;
							}
						}
					}
			    	VR.add(content);
		    	}else{
		    		for(int i=0; i < ER.size(); i++){
						if(ER.get(i).getID() == content.getID()){
							if(set.equals(SET.OR)){
								if(ER.get(i).getOR_Counter() >= content.getOR_Counter()){
									return;
								}
							}else if (set.equals(SET.Graph)){
								if(ER.get(i).getOR_Counter() == content.getOR_Counter()){
									return;
								}
							}else{
								return;
							}
						}
					}
			    	ER.add(content);
		    	}
		    
		    }
		  };
  
  ClassMatchedHandler handleHistoryRequestGraph
  = new ClassMatchedHandler<HistoryRequest, KContentMsg<?, ?, HistoryRequest>>() {

    @Override
    public void handle(HistoryRequest content, KContentMsg<?, ?, HistoryRequest> container) {
    	//If Vertex node else Edge node
    	if(id < 10){
    		trigger(container.answer(new HistoryResponse(Integer.valueOf(selfAdr.getId().toString()), VA, VR)), networkPort);
			if(!VA.isEmpty()){
				//LOG.info("{} Sending History with <nid:{}>", logPrefix, VA.get(0).getVertex().getID());
			}
			
    	}else{
    		trigger(container.answer(new HistoryResponse(Integer.valueOf(selfAdr.getId().toString()), EA, ER)), networkPort);
    	}
    }
  };
  
  ClassMatchedHandler handleHistoryResponseGraph
  = new ClassMatchedHandler<HistoryResponse, KContentMsg<?, ?, HistoryResponse>>() {

    @Override
    public void handle(HistoryResponse content, KContentMsg<?, ?, HistoryResponse> container) {
    	List<ADD> peerAddHistory = content.getAddHistory();    
    	boolean first = true;
    	for(int i =0; i < peerAddHistory.size(); i++){
    		ADD gossip = peerAddHistory.get(i);
    		if(id < 10 && gossip.getVertex() != null){
	    		if(! addExistsIn(gossip.getID(), VA, gossip.getOR_Counter())){
	    			//Check if gossip has been removed
	    			if(! removeExistsIn(gossip.getID(), VR, gossip.getOR_Counter())){
	    				if(first){
	    					//LOG.info("{} Updating Add History with <nid:{}>", logPrefix, content.getSrcID());
	    					//printGossipList(peerAddHistory, null, "<nid:"+content.getSrcID()+">");
	    					//printGossipList(pastAdd, pastRemove, logPrefix);
	        				first = false;
	        			}
	    				VA.add(gossip);
	    				trigger(gossip, gbebPort);
	    			}    			
	    		}
    		} else if(id < 10 && gossip.getEdge() != null){
    			gossip = peerAddHistory.get(peerAddHistory.size()-1);
				//Create a remove message
				if(!hasCreatedMsg){
					//LOG.info("{} Updating VR History with <nid:{}>", logPrefix, content.getSrcID());
    				REMOVE remove = new REMOVE(gossip.getSrc(), gossip.getID());
    				remove.setVertex(gossip.getEdge().getVertexU());
    				remove.setOR_Counter(1337);
    				VR.add(remove);
    				trigger(remove, gbebPort);
    				hasCreatedMsg = true;
				}
				break;
    		} else if(id > 10 && gossip.getVertex() != null){
    			//Create a remove message
    			int historySize = peerAddHistory.size();
				if(!hasCreatedMsg && historySize > 2){
					//LOG.info("{} Updating AE History with <nid:{}>", logPrefix, content.getSrcID());
					int firstIndex = randomInt(historySize-1);
					int secondIndex = randomInt(historySize-1);
					if(firstIndex == secondIndex && firstIndex == historySize-1){
						secondIndex -=1;
					}else if(firstIndex == secondIndex){
						secondIndex +=1;
					}
					ADD add = new ADD(gossip.getSrc(), gossip.getID());
					EDGE edge = new EDGE(peerAddHistory.get(firstIndex).getVertex(), peerAddHistory.get(secondIndex).getVertex());
					add.setEdge(edge);
					add.setOR_Counter(1337);
					EA.add(add);
					trigger(add, gbebPort);
					hasCreatedMsg = true;
				}
				break;
    		}else if(id > 10 && gossip.getEdge() != null){
	    		if(! addExistsIn(gossip.getID(), EA, gossip.getOR_Counter())){
	    			//Check if gossip has been removed
	    			if(! removeExistsIn(gossip.getID(), ER, gossip.getOR_Counter())){
	    				if(first){
	    					//LOG.info("{} Updating Add History with <nid:{}>", logPrefix, content.getSrcID());
	    					//printGossipList(peerAddHistory, null, "<nid:"+content.getSrcID()+">");
	    					//printGossipList(pastAdd, pastRemove, logPrefix);
	        				first = false;
	        			}
	    				EA.add(gossip);
	    				trigger(gossip, gbebPort);
	    			}    			
	    		}
    		}
    	}
    	
    	List<REMOVE> peerRemoveHistory = content.getRemoveHistory();    
    	first = true;
    	for(int i =0; i < peerRemoveHistory.size(); i++){
    		REMOVE g = peerRemoveHistory.get(i);
    		if(id < 10 && g.getEdge() != null){
    			break;
    		}else if(id > 10 && g.getVertex() != null){
    			if(! removeExistsIn(g.getID(), VR, g.getOR_Counter())){
	    			boolean done = false;
	    			for(ADD a : VA){
	    				if(a.getID() == g.getID() && a.getOR_Counter() > g.getOR_Counter()){
	    					done = true;
	    				}
	    			}
	    			if(done){
	    				VR.add(g);
	    				continue;
	    			}
	    			if(first){
	    				//LOG.info("{} Updating Remove History with <nid:{}>", logPrefix, content.getSrcID());
	    				//printGossipList(null, peerRemoveHistory, "<nid:"+content.getSrcID()+">");
						//printGossipList(pastAdd, pastRemove, logPrefix);
	    				
	    				first = false;
	    			}
	    			VR.add(g);
	    			for(int j=0; j < VA.size(); j++){
	    				if(VA.get(j).getID() == g.getID()){
	  					  VA.remove(j);
	  					  break;
	  				  }
	    			}
	    			trigger(g, gbebPort);
	    		}
    		}else if(id < 10 && g.getVertex() != null){
	    		if(! removeExistsIn(g.getID(), VR, g.getOR_Counter())){
	    			boolean done = false;
	    			for(ADD a : VA){
	    				if(a.getID() == g.getID() && a.getOR_Counter() > g.getOR_Counter()){
	    					done = true;
	    				}
	    			}
	    			if(done){
	    				VR.add(g);
	    				continue;
	    			}
	    			if(first){
	    				//LOG.info("{} Updating Remove History with <nid:{}>", logPrefix, content.getSrcID());
	    				//printGossipList(null, peerRemoveHistory, "<nid:"+content.getSrcID()+">");
						//printGossipList(pastAdd, pastRemove, logPrefix);
	    				
	    				first = false;
	    			}
	    			VR.add(g);
	    			for(int j=0; j < VA.size(); j++){
	    				if(VA.get(j).getID() == g.getID()){
	  					  VA.remove(j);
	  					  break;
	  				  }
	    			}
	    			trigger(g, gbebPort);
	    		}
    		}else if(id > 10 && g.getEdge() != null){
	    		if(! removeExistsIn(g.getID(), ER, g.getOR_Counter())){
	    			boolean done = false;
	    			for(ADD a : EA){
	    				if(a.getID() == g.getID() && a.getOR_Counter() > g.getOR_Counter()){
	    					done = true;
	    				}
	    			}
	    			if(done){
	    				ER.add(g);
	    				continue;
	    			}
	    			if(first){
	    				//LOG.info("{} Updating Remove History with <nid:{}>", logPrefix, content.getSrcID());
	    				//printGossipList(null, peerRemoveHistory, "<nid:"+content.getSrcID()+">");
						//printGossipList(pastAdd, pastRemove, logPrefix);
	    				
	    				first = false;
	    			}
	    			ER.add(g);
	    			for(int j=0; j < EA.size(); j++){
	    				if(EA.get(j).getID() == g.getID()){
	  					  EA.remove(j);
	  					  break;
	  				  }
	    			}
	    			trigger(g, gbebPort);
	    		}
    		}
    	}
    }
  };
  
  private int randomInt(int MaxValue){
	  return 0 + (int)(Math.random() * MaxValue); 
  }
  
  private boolean addExistsIn(int id, List<ADD> past, int OR_Counter){
	  //Return true if ID and OR_Counter matches element in past.
	  for(int i=0; i < past.size(); i++){
		  if(past.get(i).getID() == id){
			  if(past.get(i).getOR_Counter() >= OR_Counter){
				  return true;
			  }
		  }
	  }
	  return false;
  }
  
  private boolean removeExistsIn(int id, List<REMOVE> past, int OR_Counter){
	  for(int i=0; i < past.size(); i++){
			if(past.get(i).getID() == id){
				if(past.get(i).getOR_Counter() >= OR_Counter){
					  return true;
				  }
			}
	  }
	  return false;
  }
  
  public static class Init extends se.sics.kompics.Init<GBEBComp> {

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
