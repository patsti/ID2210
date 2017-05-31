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

import se.kth.croupier.util.CroupierHelper;
import se.kth.system.HostMngrComp.Init.SET;
import se.kth.app.test.HistoryRequest;
import se.kth.app.test.HistoryResponse;
import se.kth.app.test.Ping;
import se.kth.app.test.Pong;
import se.kth.app.test.REMOVE;
import se.kth.app.test.VERTEX;
import se.kth.app.port.CRBPort;
import se.kth.app.port.RBPort;
import se.kth.app.test.ADD;
import se.kth.app.test.EDGE;
import se.sics.kompics.ClassMatchedHandler;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Positive;
import se.sics.kompics.Start;
import se.sics.kompics.network.Network;
import se.sics.kompics.network.Transport;
import se.sics.kompics.timer.Timer;
import se.sics.ktoolbox.croupier.CroupierPort;
import se.sics.ktoolbox.croupier.event.CroupierSample;
import se.sics.ktoolbox.util.identifiable.Identifier;
import se.sics.ktoolbox.util.network.KAddress;
import se.sics.ktoolbox.util.network.KContentMsg;
import se.sics.ktoolbox.util.network.KHeader;
import se.sics.ktoolbox.util.network.basic.BasicContentMsg;
import se.sics.ktoolbox.util.network.basic.BasicHeader;


public class AppComp extends ComponentDefinition {

  private static final Logger LOG = LoggerFactory.getLogger(AppComp.class);
  private String logPrefix = " ";

  //*******************************CONNECTIONS********************************
  Positive<CRBPort> crbPort = requires(CRBPort.class);
  //**************************************************************************
  private KAddress selfAdr;
  private int id;
  private List<Integer> store;
  private List<EDGE> edgeStore;
  private List<VERTEX> vertexStore;
  private SET set;
  private int msgSentCounter = 0;

  public AppComp(Init init) {
    selfAdr = init.selfAdr;
    id = Integer.valueOf(selfAdr.getId().toString());
    logPrefix = "<nid:" + selfAdr.getId() + ">";
    set = init.set;

    subscribe(handleStart, control);
    if(!set.equals(SET.Graph)){
    	store = new ArrayList();
	    subscribe(handleAddCRB, crbPort);
	    subscribe(handleRemoveCRB, crbPort);
    }else{
    	edgeStore = new ArrayList();
    	vertexStore = new ArrayList();
    	subscribe(handleGraphAddCRB, crbPort);
	    subscribe(handleGraphRemoveCRB, crbPort);
    }
  }

  Handler handleStart = new Handler<Start>() {
    @Override
    public void handle(Start event) {
      int id = Integer.valueOf(selfAdr.getId().toString());
      if(!set.equals(SET.Graph)){
	      /* If 2 Phase and node-id is above 10, remove received message */
		  if(id >= 10){
			  LOG.info("{} Send Remove {}", logPrefix, id-10);
			  REMOVE rm = new REMOVE(id, id-10);
			  trigger(rm, crbPort);
		  }else{
		      ADD g = new ADD(id, id);
		      store.add(id);
		      trigger(g, crbPort);
		  }
      }else{
    	  if(id < 10){
    		  int i = randomInt();
    		  ADD add = new ADD(id, i);
    		  VERTEX v = new VERTEX(id);
    		  add.setVertex(v);
    		  vertexStore.add(v);
    		  trigger(add, crbPort);
    	  }
      }
    }
  };
  
  Handler handleAddCRB = new Handler<ADD>() {
	  @Override
	  public void handle(ADD msg) {
		  if(store.contains(msg.getID())){
			  store.remove((Object) msg.getID());
		  }
		  store.add(msg.getID());
		  printStore(store);
	  }
  };
  
  Handler handleRemoveCRB = new Handler<REMOVE>() {
	  @Override
	  public void handle(REMOVE msg) {
		  LOG.info("{} Removing {}", logPrefix, msg.getID());
		  store.remove((Object) msg.getID());
		  
		  int id = Integer.valueOf(selfAdr.getId().toString());
		  if(set.equals(SET.OR) && id == msg.getID()){
			  LOG.info("{} Adding {} back", logPrefix, msg.getID());
			  ADD g = new ADD(id, id);
		      g.setOR_Counter(1337);
		      store.add(id);
		      trigger(g, crbPort);
		  }
		  printStore(store);
	  }
  };
  
 
  Handler handleGraphAddCRB = new Handler<ADD>() {
	  @Override
	  public void handle(ADD msg) {		  
		  if(msg.getEdge() != null){
			  EDGE edge = msg.getEdge();
			  VERTEX u = edge.getVertexU();
			  VERTEX v = edge.getVertexV();
			  if(vertexExists(v)) return;
			  if(vertexExists(u)) return;
			  
			  if(!edgeExists(edge)){
				  edgeStore.add(edge);
				  
				  
				  if(msg.getOR_Counter() == 1337){
					  trigger(msg, crbPort);
				  }
				  printGraph(edgeStore, null);
			  }
		  }else{
			  VERTEX vertex = msg.getVertex();
			  vertexStore.add(vertex);
			  printGraph(null, vertexStore);
		  }
	  }
  };
  
  Handler handleGraphRemoveCRB = new Handler<REMOVE>() {
	  @Override
	  public void handle(REMOVE msg) {
		  if(msg.getEdge() != null){
			  EDGE edge = msg.getEdge();
			  edgeStore.remove(edge);
		  }else if(id > 10 ){
			  VERTEX vertex = msg.getVertex();
			  vertexStore.add(vertex);
			  removeEdgeWithVertex(vertex);
		  }else if(id < 10 ){
			  VERTEX vertex = msg.getVertex();
			  removeVertexFromID(vertex.getID());
			  //if 1337, generated for simulation purpose
			  if(msg.getOR_Counter() == 1337){
				  trigger(msg, crbPort);
			  }
		  }
		  if(id < 10){
			  printGraph(null, vertexStore);
		  }else{
			  printGraph(edgeStore, null);
		  }
	  }
  };
  
  private VERTEX getVertex(int id){
	  for(VERTEX vertex : vertexStore){
		  if(vertex.getID() == id){
			  return vertex;
		  }
	  }
	return null;
  }
  
  private boolean vertexExists(VERTEX v){
	  int int_v = v.getID();
	  for(VERTEX vertex : vertexStore){		  
		  int int_u = vertex.getID();		  
		  if(int_u == int_v){
			  return true;
		  }
	  }
	  return false;
  }
  
  private boolean edgeExists(EDGE e){
	  for(EDGE edge : edgeStore){
		  int eu = e.getVertexU().getID();
		  int ev = e.getVertexV().getID();
		  
		  int u = edge.getVertexU().getID();
		  int v = edge.getVertexV().getID();
		  
		  
		  if(eu == u && ev == v){
			  return true;
		  }else if (eu == v && ev == u){
			  return true;
		  }
	  }
	  return false;
  }
  
  private void removeVertexFromID(int id){
	  for(VERTEX vertex : vertexStore){
		  if(vertex.getID() == id){
			  vertexStore.remove(vertex);
			  break;
		  }
	  }
  }
  
  private void removeEdgeWithVertex(VERTEX w){
	  List<EDGE> removeThis = new ArrayList();
	  for(EDGE e : edgeStore){
		  int u = e.getVertexU().getID();
		  int v = e.getVertexV().getID();
		  if(u == w.getID() || v == w.getID()){
			  LOG.info("{} Removing edge ({},{})", logPrefix, u, v);
			  removeThis.add(e);
			  int id = Integer.valueOf(selfAdr.getId().toString());
			  REMOVE r = new REMOVE(id, randomInt());
			  r.setEdge(e);
			  trigger(r, crbPort);
		  }	  
	  }
	  edgeStore.removeAll(removeThis);
  }
  
  private void printStore(List<Integer> store){
	  String s = "";
	  for(int i=0; i< store.size(); i++){
		  int id = store.get(i);
		  if(i == 0){
			  s += String.valueOf(id);
		  }else{
			  s += ", "+String.valueOf(id);
  		  }
	  }
	  LOG.info("{} Store = [" + s + "]", logPrefix);
  }
  
  private void printGraph(List<EDGE> eStore, List<VERTEX> vStore){
	  
	  String s = "";
	  if(eStore != null){
		  for(EDGE  edge : eStore){
			  if(edge.equals(eStore.get(0))){
				  int u = edge.getVertexU().getID();
				  int v = edge.getVertexV().getID();
				  s += "(" + String.valueOf(u) +" , " + String.valueOf(v) + ")";
			  }else{
				  s += ", (" + String.valueOf(edge.getVertexU().getID()) +" , " + String.valueOf(edge.getVertexV().getID())+")";
			  }
		  }
		  LOG.info("{} EDGES = [" + s + "]", logPrefix);
	  }
	  s = "";
	  if(vStore != null){
		  for(VERTEX vertex : vStore){
			  if(vertex.equals(vStore.get(0))){
				  s += String.valueOf(vertex.getID());
			  }else{
				  s += ", "+String.valueOf(vertex.getID());
			  }
		  }
		  LOG.info("{} VERTICES = [" + s + "]", logPrefix);
	  }
  }
  
  private int randomInt(){
	  return 1 + (int)(Math.random() * 10000); 
  }
    
  public static class Init extends se.sics.kompics.Init<AppComp> {

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
