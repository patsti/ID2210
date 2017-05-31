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
package se.kth.app.test;

import java.util.ArrayList;
import java.util.List;

import se.sics.kompics.KompicsEvent;


public class ADD implements KompicsEvent{
	private int src;
	private int ID;
	private int OR_Counter;
	private List<ADD> past;
	private EDGE edge;
	private VERTEX vertex;
	
	
	public ADD(int src, int id){
		this.src = src;
		ID = id;
		past = new ArrayList();
		OR_Counter = -1;
		edge = null;
		vertex = null;
	}

	public int getID() {
		return ID;
	}

	public void setID(int Id) {
		ID = Id;
	}

	public List<ADD> getPast() {
		return past;
	}

	public void setPast(List<ADD> past) {
		this.past = past;
	}

	public int getSrc() {
		return src;
	}

	public void setSrc(int src) {
		this.src = src;
	}

	public int getOR_Counter() {
		return OR_Counter;
	}

	public void setOR_Counter(int oR_Counter) {
		OR_Counter = oR_Counter;
	}

	public EDGE getEdge() {
		return edge;
	}

	public void setEdge(EDGE edge) {
		this.edge = edge;
	}

	public VERTEX getVertex() {
		return vertex;
	}

	public void setVertex(VERTEX vertex) {
		this.vertex = vertex;
	}
	
	
}
