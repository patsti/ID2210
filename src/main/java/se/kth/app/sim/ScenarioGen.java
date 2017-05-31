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
package se.kth.app.sim;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.kth.app.CRBComp;
import se.kth.sim.compatibility.SimNodeIdExtractor;
import se.kth.system.HostMngrComp;
import se.sics.kompics.network.Address;
import se.sics.kompics.simulator.SimulationScenario;
import se.sics.kompics.simulator.adaptor.Operation;
import se.sics.kompics.simulator.adaptor.Operation1;
import se.sics.kompics.simulator.adaptor.distributions.extra.BasicIntSequentialDistribution;
import se.sics.kompics.simulator.events.system.KillNodeEvent;
import se.sics.kompics.simulator.events.system.SetupEvent;
import se.sics.kompics.simulator.events.system.StartNodeEvent;
import se.sics.kompics.simulator.network.identifier.IdentifierExtractor;
import se.sics.ktoolbox.omngr.bootstrap.BootstrapServerComp;
import se.sics.ktoolbox.util.network.KAddress;

public class ScenarioGen {

    static Operation<SetupEvent> systemSetupOp = new Operation<SetupEvent>() {
        @Override
        public SetupEvent generate() {
            return new SetupEvent() {
                @Override
                public IdentifierExtractor getIdentifierExtractor() {
                    return new SimNodeIdExtractor();
                }
            };
        }
    };

    static Operation<StartNodeEvent> startBootstrapServerOp = new Operation<StartNodeEvent>() {

        @Override
        public StartNodeEvent generate() {
            return new StartNodeEvent() {
                KAddress selfAdr;

                {
                    selfAdr = ScenarioSetup.bootstrapServer;
                }

                @Override
                public Address getNodeAddress() {
                    return selfAdr;
                }

                @Override
                public Class getComponentDefinition() {
                    return BootstrapServerComp.class;
                }

                @Override
                public BootstrapServerComp.Init getComponentInit() {
                    return new BootstrapServerComp.Init(selfAdr);
                }
            };
        }
    };

    static Operation1<StartNodeEvent, Integer> startGrowOnlyNodeOp = new Operation1<StartNodeEvent, Integer>() {

        @Override
        public StartNodeEvent generate(final Integer nodeId) {
            return new StartNodeEvent() {
                KAddress selfAdr;

                {
                    String nodeIp = "193.0.0." + nodeId;
                    selfAdr = ScenarioSetup.getNodeAdr(nodeIp, nodeId);
                }

                @Override
                public Address getNodeAddress() {
                    return selfAdr;
                }

                @Override
                public Class getComponentDefinition() {
                    return HostMngrComp.class;
                }

                @Override
                public HostMngrComp.Init getComponentInit() {
                    return new HostMngrComp.Init(selfAdr, ScenarioSetup.bootstrapServer, ScenarioSetup.croupierOId, "G_Set");
                }

                @Override
                public Map<String, Object> initConfigUpdate() {
                    Map<String, Object> nodeConfig = new HashMap<>();
                    nodeConfig.put("system.id", nodeId);
                    nodeConfig.put("system.seed", ScenarioSetup.getNodeSeed(nodeId));
                    nodeConfig.put("system.port", ScenarioSetup.appPort);
                    return nodeConfig;
                }
            };
        }
    };
    
    static Operation1<StartNodeEvent, Integer> start2PNodeOp = new Operation1<StartNodeEvent, Integer>() {

        @Override
        public StartNodeEvent generate(final Integer nodeId) {
            return new StartNodeEvent() {
                KAddress selfAdr;

                {
                    String nodeIp = "193.0.0." + nodeId;
                    selfAdr = ScenarioSetup.getNodeAdr(nodeIp, nodeId);
                }

                @Override
                public Address getNodeAddress() {
                    return selfAdr;
                }

                @Override
                public Class getComponentDefinition() {
                    return HostMngrComp.class;
                }

                @Override
                public HostMngrComp.Init getComponentInit() {
                    return new HostMngrComp.Init(selfAdr, ScenarioSetup.bootstrapServer, ScenarioSetup.croupierOId, "TwoP_Set");
                }

                @Override
                public Map<String, Object> initConfigUpdate() {
                    Map<String, Object> nodeConfig = new HashMap<>();
                    nodeConfig.put("system.id", nodeId);
                    nodeConfig.put("system.seed", ScenarioSetup.getNodeSeed(nodeId));
                    nodeConfig.put("system.port", ScenarioSetup.appPort);
                    return nodeConfig;
                }
            };
        }
    };
    
    static Operation1<StartNodeEvent, Integer> startORNodeOp = new Operation1<StartNodeEvent, Integer>() {

        @Override
        public StartNodeEvent generate(final Integer nodeId) {
            return new StartNodeEvent() {
                KAddress selfAdr;

                {
                    String nodeIp = "193.0.0." + nodeId;
                    selfAdr = ScenarioSetup.getNodeAdr(nodeIp, nodeId);
                }

                @Override
                public Address getNodeAddress() {
                    return selfAdr;
                }

                @Override
                public Class getComponentDefinition() {
                    return HostMngrComp.class;
                }

                @Override
                public HostMngrComp.Init getComponentInit() {
                    return new HostMngrComp.Init(selfAdr, ScenarioSetup.bootstrapServer, ScenarioSetup.croupierOId, "OR_Set");
                }

                @Override
                public Map<String, Object> initConfigUpdate() {
                    Map<String, Object> nodeConfig = new HashMap<>();
                    nodeConfig.put("system.id", nodeId);
                    nodeConfig.put("system.seed", ScenarioSetup.getNodeSeed(nodeId));
                    nodeConfig.put("system.port", ScenarioSetup.appPort);
                    return nodeConfig;
                }
            };
        }
    };
    
    static Operation1<StartNodeEvent, Integer> startGraphNodeOp = new Operation1<StartNodeEvent, Integer>() {

        @Override
        public StartNodeEvent generate(final Integer nodeId) {
            return new StartNodeEvent() {
                KAddress selfAdr;

                {
                    String nodeIp = "193.0.0." + nodeId;
                    selfAdr = ScenarioSetup.getNodeAdr(nodeIp, nodeId);
                }

                @Override
                public Address getNodeAddress() {
                    return selfAdr;
                }

                @Override
                public Class getComponentDefinition() {
                    return HostMngrComp.class;
                }

                @Override
                public HostMngrComp.Init getComponentInit() {
                    return new HostMngrComp.Init(selfAdr, ScenarioSetup.bootstrapServer, ScenarioSetup.croupierOId, "Graph");
                }

                @Override
                public Map<String, Object> initConfigUpdate() {
                    Map<String, Object> nodeConfig = new HashMap<>();
                    nodeConfig.put("system.id", nodeId);
                    nodeConfig.put("system.seed", ScenarioSetup.getNodeSeed(nodeId));
                    nodeConfig.put("system.port", ScenarioSetup.appPort);
                    return nodeConfig;
                }
            };
        }
    };
    
    static Operation1 killNode = new Operation1<KillNodeEvent, Integer>() {

        @Override
        public KillNodeEvent generate(final Integer nodeId) {

            return new KillNodeEvent() {
                private KAddress killerAddress;

                //this constructor is killer
                {
                    try{
                    	String nodeIp = "193.0.0." + nodeId;
                    	killerAddress = ScenarioSetup.getNodeAdr(nodeIp, nodeId);
                    	final Logger LOG = LoggerFactory.getLogger(ScenarioGen.class);
                        LOG.info("terminate: {}", nodeIp);
                    }catch (Exception e){
                    	e.printStackTrace();
                    }
                }

                @Override
                public Address getNodeAddress() {
                    return killerAddress;
                }
            };
        }
    };

	public static SimulationScenario simpleBoot(final int set) {
        SimulationScenario scen = new SimulationScenario() {
            {
                StochasticProcess systemSetup = new StochasticProcess() {
                    {
                        eventInterArrivalTime(constant(1000));
                        raise(1, systemSetupOp);
                    }
                };
                StochasticProcess startBootstrapServer = new StochasticProcess() {
                    {
                        eventInterArrivalTime(constant(1000));
                        raise(1, startBootstrapServerOp);
                    }
                };
                StochasticProcess startPeers = new StochasticProcess() {
                    {
                        eventInterArrivalTime(uniform(1000, 1100));
                        if(set == 0){
                			raise(5, startGrowOnlyNodeOp, new BasicIntSequentialDistribution(1));
                		}else if(set == 1){
                			raise(5, start2PNodeOp, new BasicIntSequentialDistribution(1));
                		}else {
                			raise(5, startORNodeOp, new BasicIntSequentialDistribution(1));
                		}
                    }
                };
                StochasticProcess startPeers2 = new StochasticProcess() {
                    {
                        eventInterArrivalTime(uniform(1000, 1100));
                        if(set == 0){
                			raise(5, startGrowOnlyNodeOp, new BasicIntSequentialDistribution(11));
                		}else if(set == 1) {
                			raise(5, start2PNodeOp, new BasicIntSequentialDistribution(11));
                		}else {
                			raise(5, startORNodeOp, new BasicIntSequentialDistribution(11));
                		}
                    }
                };
                
                systemSetup.start();
                startBootstrapServer.startAfterTerminationOf(1000, systemSetup);
                //Starting peers that put data into the network
                startPeers.startAfterTerminationOf(1000, startBootstrapServer);
                //Starting peers that removes data from the network
                startPeers2.startAfterTerminationOf(1000, startPeers);
                terminateAfterTerminationOf(1000*100, startPeers2);
            }
        };

        return scen;
    }
	
	public static SimulationScenario simulateGraph2P2P(final int set) {
        SimulationScenario scen = new SimulationScenario() {
            {
            	final int numberOfNodes = 3;
                StochasticProcess systemSetup = new StochasticProcess() {
                    {
                        eventInterArrivalTime(constant(1000));
                        raise(1, systemSetupOp);
                    }
                };
                StochasticProcess startBootstrapServer = new StochasticProcess() {
                    {
                        eventInterArrivalTime(constant(1000));
                        raise(1, startBootstrapServerOp);
                    }
                };
                StochasticProcess startVertexPeers = new StochasticProcess() {
                    {
                        eventInterArrivalTime(uniform(1000, 1100));
                		raise(numberOfNodes, startGraphNodeOp, new BasicIntSequentialDistribution(1));
                		
                    }
                };
                StochasticProcess startEdgePeers = new StochasticProcess() {
                    {
                        eventInterArrivalTime(uniform(1000, 1100));
                		raise(numberOfNodes, startGraphNodeOp, new BasicIntSequentialDistribution(11));
                		
                    }
                };
                
                systemSetup.start();
                startBootstrapServer.startAfterTerminationOf(1000, systemSetup);
                //Starting peers that put data into the network
                startVertexPeers.startAfterTerminationOf(1000, startBootstrapServer);
                //Starting peers that removes data from the network
                startEdgePeers.startAfterTerminationOf(1000, startVertexPeers);
                terminateAfterTerminationOf(1000*10000, startEdgePeers);
            }
        };

        return scen;
    }
    
    public static SimulationScenario simulateChurn1(final int set) {
        SimulationScenario scen = new SimulationScenario() {
            {
                StochasticProcess systemSetup = new StochasticProcess() {
                    {
                        eventInterArrivalTime(constant(1000));
                        raise(1, systemSetupOp);
                    }
                };
                StochasticProcess startBootstrapServer = new StochasticProcess() {
                    {
                        eventInterArrivalTime(constant(1000));
                        raise(1, startBootstrapServerOp);
                    }
                };
                StochasticProcess startPeers = new StochasticProcess() {
                	{
                		eventInterArrivalTime(uniform(1000, 1100));
                		if(set == 0){
                			raise(5, startGrowOnlyNodeOp, new BasicIntSequentialDistribution(1));
                		}else if(set == 1) {
                			raise(5, start2PNodeOp, new BasicIntSequentialDistribution(1));
                		}else {
                			raise(5, startORNodeOp, new BasicIntSequentialDistribution(1));
                		}
                	}
                };
                StochasticProcess startChurnPeers = new StochasticProcess() {
                    {
                        eventInterArrivalTime(uniform(1000, 1100));
                        if(set == 0){
                			raise(1, startGrowOnlyNodeOp, new BasicIntSequentialDistribution(11));
                		}else if(set == 1){
                			raise(1, start2PNodeOp, new BasicIntSequentialDistribution(11));
                		}else {
                			raise(1, startORNodeOp, new BasicIntSequentialDistribution(11));
                		}
                    }
                };
                StochasticProcess killChurn = new SimulationScenario.StochasticProcess(){
                    //nested constructor ;o
                    {
                        eventInterArrivalTime(constant(1000));
                        raise(1, killNode, new BasicIntSequentialDistribution(11));
                    }
                };
                systemSetup.start();
                startBootstrapServer.startAfterTerminationOf(1000, systemSetup);
                startPeers.startAfterTerminationOf(1000, startBootstrapServer);
                startChurnPeers.startAfterTerminationOf(1000, startPeers);
                killChurn.startAfterTerminationOf(6000, startPeers);
                
                terminateAfterTerminationOf(1000*100, killChurn);
            }
        };

        return scen;
    }
    
    public static SimulationScenario simulateChurn2(final int set) {
        SimulationScenario scen = new SimulationScenario() {
            {
                StochasticProcess systemSetup = new StochasticProcess() {
                    {
                        eventInterArrivalTime(constant(1000));
                        raise(1, systemSetupOp);
                    }
                };
                StochasticProcess startBootstrapServer = new StochasticProcess() {
                    {
                        eventInterArrivalTime(constant(1000));
                        raise(1, startBootstrapServerOp);
                    }
                };
                StochasticProcess startPeers = new StochasticProcess() {
                	{
                		eventInterArrivalTime(uniform(1000, 1100));
                		if(set == 0){
                			raise(3, startGrowOnlyNodeOp, new BasicIntSequentialDistribution(1));
                		}else if (set == 1){
                			raise(3, start2PNodeOp, new BasicIntSequentialDistribution(1));
                		}else {
                			raise(3, startORNodeOp, new BasicIntSequentialDistribution(1));
                		}
                	}
                };
                StochasticProcess startChurnPeers = new StochasticProcess() {
                    {
                        eventInterArrivalTime(uniform(1000, 1100));
                        if(set == 0){
                			raise(5, startGrowOnlyNodeOp, new BasicIntSequentialDistribution(11));
                		}else if(set == 1){
                			raise(5, start2PNodeOp, new BasicIntSequentialDistribution(11));
                		}else {
                			raise(5, startORNodeOp, new BasicIntSequentialDistribution(11));
                		}
                    }
                };
                StochasticProcess killChurn = new SimulationScenario.StochasticProcess(){
                    //nested constructor ;o
                    {
                        eventInterArrivalTime(constant(1000));
                        raise(5, killNode, new BasicIntSequentialDistribution(11));
                    }
                };
                systemSetup.start();
                startBootstrapServer.startAfterTerminationOf(1000, systemSetup);
                startPeers.startAfterTerminationOf(1000, startBootstrapServer);
                startChurnPeers.startAfterTerminationOf(1000, startPeers);
                killChurn.startAfterTerminationOf(6000, startPeers);
                
                terminateAfterTerminationOf(1000*100, killChurn);
            }
        };

        return scen;
    }
    
}
