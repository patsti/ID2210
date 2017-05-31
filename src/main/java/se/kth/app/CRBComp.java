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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.kth.app.port.CRBPort;
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

public class CRBComp extends ComponentDefinition {

  private final Logger LOG = LoggerFactory.getLogger(CRBComp.class);
  private String logPrefix = " ";

  //*******************************CONNECTIONS********************************
  Positive<RBPort> rbPort = requires(RBPort.class);
  Negative<CRBPort> crbPort = provides(CRBPort.class);
  //**************************************************************************
  private KAddress selfAdr;
  private List<ADD> delivered;
  private List<ADD> past;
  private SET set;
  
  public CRBComp(Init init) {
    selfAdr = init.selfAdr;
    delivered = new ArrayList();
    past = new ArrayList();
    set = init.set;
    logPrefix = "<nid:" + selfAdr.getId() + ">";
    

    subscribe(handleStart, control);
    subscribe(handleAddCRB, crbPort);
    subscribe(handleRemoveCRB, crbPort);
    subscribe(handleAddRB, rbPort);
    subscribe(handleRemoveRB, rbPort);
    
  }

  Handler handleStart = new Handler<Start>() {
    @Override
    public void handle(Start event) {
    }
  };
  
  Handler handleAddCRB = new Handler<ADD>() {
	  @Override
	  public void handle(ADD gossip) {
		  //if Observed-Removed set, implement counter to message to help with casual order.
		  if(set.equals(SET.OR) || set.equals(SET.Graph)){
			  int counter = 0;
			  for(ADD e : delivered){
				  if(e.getID() == gossip.getID()){
					  counter++;
				  }
			  }
			  gossip.setOR_Counter(counter);
		  }
		  past.add(gossip);
		  delivered.add(gossip);
		  trigger(gossip, rbPort);
	  }
  };
  
  Handler handleRemoveCRB = new Handler<REMOVE>() {
	    @Override
	    public void handle(REMOVE gossip) {
	    	//if Observed-Removed set, implement counter to message to help with casual order.
	    	if(set.equals(SET.OR) || set.equals(SET.Graph)){
				  int counter = 0;
				  for(ADD e : delivered){
					  if(e.getID() == gossip.getID()){
						  counter++;
					  }
				  }
				  gossip.setOR_Counter(counter);
			}
			trigger(gossip, rbPort);
	    }
  };
  
  Handler handleAddRB = new Handler<ADD>() {
	  @Override
	  public void handle(ADD msg) {
		  if(!addExistsIn(msg.getID(), delivered, msg.getOR_Counter())){
			  for(ADD n : msg.getPast()){
				  if(!delivered.contains(n)){
					  delivered.add(n);
					  if(!past.contains(n)){
						  past.add(n);
					  }
				  }
			  }
			  delivered.add(msg);
			  trigger(msg, crbPort);
			  if(!past.contains(msg)){
				  past.add(msg);
			  }
		  }
	  }
  };
  
  Handler handleRemoveRB = new Handler<REMOVE>() {
	  @Override
	  public void handle(REMOVE msg) {
			  for(int j=0; j < past.size(); j++){
					if(past.get(j).getID() == msg.getID()){
						past.remove(j);
						break;
					}
			  }
			  trigger(msg, crbPort);
	  }
  };
  
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
  
  private void printGossipList(List<ADD> history){
	  String s = "";
	  for(ADD gos : history){
		  if(gos.equals(history.get(0))){
			  s += String.valueOf(gos.getID());
		  }else{
			  s += ", "+String.valueOf(gos.getID());
		  }
	  }
	  LOG.info("{} History list = [" + s + "]", logPrefix);
  }
  
  public static class Init extends se.sics.kompics.Init<CRBComp> {

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
