import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Stack;
import java.util.TreeMap;

public class MainClass {

	static final int DIST_LIMIT = 30;
	// Store all nodes in HashMap for quickly access all nodes
	static HashMap<Integer, Node> allNodesHM;
	// Keep track of all edges, key is the weight of edge, value is HashSet of Pair(vNum1,vNum2)
	static TreeMap<Integer, HashSet<Pair>> weightEdgeTreeMap;
	// Keep track of all the gasStation
	static HashSet<Integer> gasStations;
	// Store all covered vertices in HashSet
	static HashSet<Integer> coveredVerticesHS = new HashSet<Integer>();

	public static void main(String[] args) throws FileNotFoundException {
		getInput();
		PrintWriter pw = new PrintWriter("outputNov10.txt");
		while (coveredVerticesHS.size() != allNodesHM.size()) {
			System.out.printf("Covered: %d, all: %d\n", coveredVerticesHS.size(), allNodesHM.size());
			findBestVertexAndPutGasStation();
			System.out.println("Num of gas stations: " + gasStations.size());
			if (allNodesHM.size() - coveredVerticesHS.size() < 12) {
				System.out.println("YOLO");
				for (Map.Entry<Integer, Node> eachNode : allNodesHM.entrySet()) {
					if (!coveredVerticesHS.contains(eachNode.getKey())) {
						coveredVerticesHS.add(eachNode.getKey());
						gasStations.add(eachNode.getKey());
					}
				}
			}
		}
		System.out.println("Done");
		for (Integer anInt : gasStations) {
			pw.print(anInt + "x");
		}
		pw.close();
	}

	public static void getInput() throws FileNotFoundException {
		gasStations = new HashSet<Integer>();
		weightEdgeTreeMap = new TreeMap<Integer, HashSet<Pair>>();
		allNodesHM = new HashMap<Integer, Node>();
		FileReader aFR = new FileReader("graphz.txt");
		Scanner sc = new Scanner(aFR);
		Scanner tempSc, tempSc2;
		String eachLine, eachLineNode;
		int vertexNum, neighborNum, weight;
		Node newNode;
		Pair newPair;

		while (sc.hasNextLine()) {
			eachLine = sc.nextLine();
			tempSc = new Scanner(eachLine);
			tempSc.useDelimiter(":");
			vertexNum = Integer.parseInt(tempSc.next());
			// System.out.println(vertexNum);
			// Initializing new Node from the input file
			newNode = new Node(vertexNum);

			eachLine = tempSc.next();
			tempSc = new Scanner(eachLine);
			tempSc.useDelimiter("]");
			while (tempSc.hasNext()) {
				eachLineNode = tempSc.next();
				tempSc2 = new Scanner(eachLineNode.replaceAll("\\[|\\]", ""));
				tempSc2.useDelimiter(",");
				neighborNum = Integer.parseInt(tempSc2.next());
				weight = Integer.parseInt(tempSc2.next());

				// Add neighbor to new Node and put the Pair in TreeMap
				newNode.addNeighbor(neighborNum, weight);
				HashSet<Pair> aHS = weightEdgeTreeMap.get(weight);
				if (aHS == null) {
					aHS = new HashSet<Pair>();
				}
				newPair = new Pair(vertexNum, neighborNum);
				aHS.add(newPair);
				weightEdgeTreeMap.put(weight, aHS);
				// Store all nodes in HashMap
				allNodesHM.put(vertexNum, newNode);
			}
		}
		sc.close();
	}

	public static void findBestVertexAndPutGasStation() {
		HashSet<Pair> minPairSet;
		HashSet<Integer> eachNumInPairHS = new HashSet<Integer>();
		HashSet<Integer> coveredElementsHS = new HashSet<Integer>();
		Stack<Node> nodeStack = new Stack<Node>();
		Stack<Integer> weightStack = new Stack<Integer>();
		int numOfNewVerticesCovered = 0, startingVertexNum = 0, currentWeight = 0, maxVerticesCovered = 0;
		Node currentNode = null, currentNeighbor, lastVisitedNode = null;
		Node.Pair neighborPair;
		BestPair theBestPair = null;

		if (weightEdgeTreeMap.firstEntry().getKey() < DIST_LIMIT) {
			minPairSet = weightEdgeTreeMap.firstEntry().getValue();
			int count = 0;
			for (Pair eachMinPair : minPairSet) {
				if (count == ((minPairSet.size() > 10) ? 10 : minPairSet.size())) {
					break;
				}
				count++;
				eachMinPair.resetEntry();
				startingVertexNum = eachMinPair.getEntry();
				while (startingVertexNum != -1) {
					if (eachNumInPairHS.add(startingVertexNum)) {
						currentNode = allNodesHM.get(startingVertexNum);
						currentNode.resetPQ();

						nodeStack.push(currentNode);
						currentNode.visited = true;
						while (!nodeStack.isEmpty()) {
							currentNode = nodeStack.peek();
							while (currentNode.hasMinNeighbor()) {
								neighborPair = currentNode.getMinNeighbor();
								currentNeighbor = allNodesHM.get(neighborPair.vertexNum);
								if (currentNeighbor.visited)
									continue;
								else if ((currentWeight + neighborPair.weightValue) > DIST_LIMIT)
									continue;
								else {
									if (!coveredVerticesHS.contains(neighborPair.vertexNum))
										numOfNewVerticesCovered++;
									weightStack.push(neighborPair.weightValue);
									currentWeight += neighborPair.weightValue;
									currentNeighbor.visited = true;
									nodeStack.push(currentNeighbor);
									coveredElementsHS.add(neighborPair.vertexNum);
									currentNode = currentNeighbor;
								}
							}
							currentNode.visited = false;
							lastVisitedNode = nodeStack.pop();
							lastVisitedNode.resetPQ();
							currentWeight -= (weightStack.isEmpty()) ? 0 : weightStack.pop();
						}
					}
					if (coveredElementsHS.size() > maxVerticesCovered && numOfNewVerticesCovered > 0) {
						maxVerticesCovered = coveredElementsHS.size();
						theBestPair = new BestPair(lastVisitedNode.vertexNum, coveredElementsHS.size());
					}
					startingVertexNum = eachMinPair.getEntry();
					coveredElementsHS = new HashSet<Integer>();
				}
			}
			System.out.printf("Count: %d, setSize: %d\n", count, minPairSet.size());
			if (theBestPair == null) {
				//				for (Pair eachPair : minPairSet) {
				//					eachPair.resetEntry();
				//					startingVertexNum = eachPair.getEntry();
				//
				//					System.out.println("VertexNum: " + startingVertexNum);
				//
				//					while (startingVertexNum != -1) {
				//						if (!coveredVerticesHS.contains(startingVertexNum)) {
				//							gasStations.add(startingVertexNum);
				//
				//							System.out.println("Placing gas Station at " + startingVertexNum);
				//
				//							coveredVerticesHS.add(startingVertexNum);
				//						}
				//						removeEdgeFromTreeMap(weightEdgeTreeMap.firstEntry().getKey(), eachPair);
				//						System.out.println("Removing pair.");
				//
				//						startingVertexNum = eachPair.getEntry();
				//					}
				//					break;
				//				}
			} else {
				putGasStation(theBestPair.vertexNum);
			}
		} else {
			minPairSet = weightEdgeTreeMap.firstEntry().getValue();
			for (Pair eachPair : minPairSet) {
				startingVertexNum = eachPair.getEntry();
				while (startingVertexNum != -1) {
					if (eachNumInPairHS.add(startingVertexNum)) {
						if (!coveredVerticesHS.contains(startingVertexNum)) {
							gasStations.add(startingVertexNum);
							coveredVerticesHS.add(startingVertexNum);
						}
					}
					startingVertexNum = eachPair.getEntry();
				}
			}
		}
	}

	public static void putGasStation(int pBestVertexNum) {
		gasStations.add(pBestVertexNum);
		coveredVerticesHS.add(pBestVertexNum);
		Stack<Node> nodeStack = new Stack<Node>();
		Stack<Integer> weightStack = new Stack<Integer>();
		Node.Pair neighborPair = null;
		Node currentNode, currentNeighbor, lastVisitedNode;
		int currentWeight = 0;

		currentNode = allNodesHM.get(pBestVertexNum);
		currentNode.resetPQ();
		nodeStack.push(currentNode);
		currentNode.visited = true;

		while (!nodeStack.isEmpty()) {
			currentNode = nodeStack.peek();
			while (currentNode.hasMinNeighbor()) {
				neighborPair = currentNode.getMinNeighbor();
				currentNeighbor = allNodesHM.get(neighborPair.vertexNum);
				if (currentNeighbor.visited)
					continue;
				else if (currentWeight + neighborPair.weightValue > DIST_LIMIT)
					continue;
				else {
					coveredVerticesHS.add(currentNeighbor.vertexNum);
					removeEdgeFromTreeMap(neighborPair.weightValue, new Pair(currentNode.vertexNum,
							currentNeighbor.vertexNum));
					weightStack.push(neighborPair.weightValue);
					currentWeight += neighborPair.weightValue;
					currentNeighbor.visited = true;
					nodeStack.push(currentNeighbor);
					currentNode = currentNeighbor;
				}
			}
			currentNode.visited = false;
			lastVisitedNode = nodeStack.pop();
			lastVisitedNode.resetPQ();
			currentWeight -= (weightStack.isEmpty()) ? 0 : weightStack.pop();
		}
	}

	public static void removeEdgeFromTreeMap(int pWeight, Pair pRemovePair) {
		HashSet<Pair> elementSet = weightEdgeTreeMap.get(pWeight);
		if (elementSet != null && elementSet.size() != 0) {
			elementSet.remove(pRemovePair);
		}
		if (elementSet != null && elementSet.size() == 0) {
			weightEdgeTreeMap.remove(pWeight);
		}
	}

	public static void printAllNodesAndNeighbors() {
		//		for (Map.Entry<Integer, HashSet<Integer>> eachEle : weightEdgeTreeMap.entrySet()) {
		//			HashSet<Integer> aHS = eachEle.getValue();
		//			for (Integer eachInt : aHS) {
		//				Node eachNode = allNodesHM.get(eachInt);
		//				System.out.printf("VertexNum: %d with neighbors: ", eachInt);
		//				while (eachNode.hasMinNeighbor()) {
		//					System.out.print(eachNode.getMinNeighbor().vertexNum + " ");
		//				}
		//				eachNode.resetPQ();
		//				System.out.println();
		//			}
		//			System.out.println();
		//		}
	}

	public static void printTreeMap() {
		for (Map.Entry<Integer, HashSet<Pair>> eachEntry : weightEdgeTreeMap.entrySet()) {
			System.out.println(eachEntry.getKey() + " : " + eachEntry.getValue().size());
			//			HashSet<Pair> aHS = eachEntry.getValue();
			//			for (Pair eachPair : aHS) {
			//				System.out.printf("(%d,%d)", eachPair.vertexNum1, eachPair.vertexNum2);
			//			}
			//			System.out.println();
		}
	}

	static class BestPair {
		int vertexNum;
		int verticesCovered;

		public BestPair(int pVertexNum, int pVerticesCovered) {
			vertexNum = pVertexNum;
			verticesCovered = pVerticesCovered;
		}
	}

	static class Pair {
		int vertexNum1;
		int vertexNum2;
		byte count;

		public Pair(int pNum1, int pNum2) {
			vertexNum1 = pNum1;
			vertexNum2 = pNum2;
			count = 0;
		}

		public void resetEntry() {
			count = 0;
		}

		public int getEntry() {
			if (count == 0) {
				count++;
				return vertexNum1;
			} else if (count == 1) {
				count++;
				return vertexNum2;
			}
			return -1;
		}

		public boolean equals(Object o) {
			Pair comPair = (Pair) o;
			boolean bool1 = (this.vertexNum1 == comPair.vertexNum1 && this.vertexNum2 == comPair.vertexNum2);
			boolean bool2 = (this.vertexNum1 == comPair.vertexNum2 && this.vertexNum2 == comPair.vertexNum1);
			return (bool1 || bool2);
		}

		public int hashCode() {
			return (new Integer(vertexNum1).hashCode()) + (new Integer(vertexNum2).hashCode());
		}
	}
}
