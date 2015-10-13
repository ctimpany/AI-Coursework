import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Random;


public class RoundTrip 
{
	
	private static String[] cities;
	private static int[][] distance;
	private static String originCity = "Edinburgh";
	
	private static String[][][] population;
	private static String[][][] intermediatePopulation;
	private static int bestRoute;
	private static double bestFitness = 0;
	
	private static int generation = 1;
	private final static int MAX_POPULATION = 10;
	private final static int MAX_GENERATIONS = 20;
	private final static double MUTATION_RATE = 0.2; //ratio of how many genes within a child will be swapped. between 0 and 1.
	private final static double CROSSOVER_RATE = 0.2; //ratio over which genes from each parent will be distributed within the child. Between 0 and 1.
	private static Random random = new Random();
	
	private static double[][] rouletteWheel = new double[MAX_POPULATION][2];
	private static double[] routeFitness = new double[MAX_POPULATION];
	private static int[] routeDistance = new int[MAX_POPULATION];
	
	/*
	 * This method takes in the file containing the distance matrix and converts it into 2 arrays.
	 * 1 containing the cities, and 1 containing the distances.
	 */
	private static void createMatrices()
	{
		try 
		{
			BufferedReader reader = new BufferedReader(new FileReader("../RoundTrip/src/Cities.txt"));
			String strIn = reader.readLine();
			cities = strIn.split("\t");
			String[] tmp;
			distance = new int[cities.length][cities.length];
			for(int i = 0; i<cities.length; i++)
			{
				strIn = reader.readLine();
				tmp = strIn.split("\t");
				for(int j = 0; j<cities.length; j++)
				{
					distance[i][j] = Integer.parseInt(tmp[j]);
				}
			}
		} 
		catch (FileNotFoundException e) 
		{
			System.out.println("That file does not exist.");
			e.printStackTrace();
		} catch (IOException e) 
		{
			System.out.println("This file is empty.");
			e.printStackTrace();
		}
	}
	
	private static void initialisePopulation()
	{
		int chromosome;
		for(int i = 0; i<MAX_POPULATION;i++)
		{
			String[] tmpCities = new String [cities.length];
			System.arraycopy(cities, 0, tmpCities, 0, cities.length);
			
			population[i][0][0] = originCity;
			population[i][0][1] = "0";
			
			population[i][cities.length][0] = originCity;
			
			for(int j = 0; j<cities.length; j++)
			{
				if(population[i][j+1][0]==null)
				{
					chromosome = random.nextInt(cities.length);
					while(tmpCities[chromosome]==null || tmpCities[chromosome].equals(originCity))
					{
						chromosome = random.nextInt(cities.length);
					}
					population[i][j+1][0] = tmpCities[chromosome];
					tmpCities[chromosome]=null;
				}
				population[i][j+1][1] = calculateDistance(population[i][j+1][0], population[i][j][0]);
				
			}
		}
	}
	
	private static String calculateDistance(String to, String from)
	{
		int distanceValue;
		int toIndex = -1;
		int fromIndex = -1;
		for(int i = 0; i<cities.length; i++)
		{
			if(cities[i].equals(to))
			{
				toIndex=i;
			}else if(cities[i].equals(from))
			{
				fromIndex=i;
			}
		}
		
		distanceValue = distance[fromIndex][toIndex];
		return Integer.toString(distanceValue);
	}
	
	private static void evaluatePopulation()
	{
		for(int i = 0; i<MAX_POPULATION; i++)
		{
			int totalDistance = 0;
			for(int j = 0; j<cities.length+1; j++)
			{
				totalDistance += Integer.parseInt(population[i][j][1]);
				
			}
			routeDistance[i] = totalDistance;
			routeFitness[i] = 1000.0 / totalDistance;
			
			if(routeFitness[i] > bestFitness)
			{
				bestFitness = routeFitness[i];
				bestRoute = i;
			}
		}
	}
	
	private static void generateNewGeneration()
	{
		for(int i = 0; i<cities.length+1; i++)
		{
			intermediatePopulation[0][i][0] = population[bestRoute][i][0];
			intermediatePopulation[0][i][1] = population[bestRoute][i][1];
		}
		//^^^ This is inserting the best solution from the previous generation into the first slot of the new generation.
		
		int parent1, parent2, ratio;
		DecimalFormat df = new DecimalFormat("#");
		
		ratio = Integer.parseInt(df.format((cities.length+1)*CROSSOVER_RATE)); 
		
		for(int i = 1; i<population.length; i++)
		{
			parent1 = rouletteWheel();
			parent2 = rouletteWheel();

			intermediatePopulation[i][0][0] = originCity;
			for(int j = 1; j<cities.length+1; j++)
			{
				if(j<=ratio)
				{
					//parent1 code
				}else
				{
					//parent2 code
				}
			}
			
			childMutation(i);
			
			//Calculate the distances between each city.
			for(int k = 0; k<cities.length+1; k++)
			{
				if(k==0)
				{
					intermediatePopulation[i][k][1] = "0";
				}else
				{
					intermediatePopulation[i][k][1] = calculateDistance(intermediatePopulation[i][k][0], intermediatePopulation[i][k-1][0]);
				}
			}
		}
		population = intermediatePopulation;
	}
	
	private static void childMutation(int placeInPopulation)
	{
		DecimalFormat df = new DecimalFormat("#");
		int ratio = Integer.parseInt(df.format((cities.length+1)*MUTATION_RATE));
		int randInt;
		String tmpCity, tmpDistance;
		
		for(int i = 0; i<ratio; i++)
		{
			randInt = random.nextInt(cities.length-2)+1;
			
			tmpCity = intermediatePopulation[placeInPopulation][randInt][0];
			tmpDistance = intermediatePopulation[placeInPopulation][randInt][1];
			
			intermediatePopulation[placeInPopulation][randInt][0] = intermediatePopulation[placeInPopulation][randInt+1][0];
			intermediatePopulation[placeInPopulation][randInt][1] = intermediatePopulation[placeInPopulation][randInt+1][1];
			
			intermediatePopulation[placeInPopulation][randInt+1][0] = tmpCity;
			intermediatePopulation[placeInPopulation][randInt+1][1] = tmpDistance;
		}
	}
	
	private static int rouletteWheel()
	{
		double totalFitness = 0.0;
		int returnInt = -1;
		for(int i = 0; i<population.length; i++)
		{
			totalFitness += routeFitness[i];
		}
		
        int randint = random.nextInt(1000001);
        double randDouble = randint / 1000000.0;
        double pointer = totalFitness * randDouble;
        double accumulatingFitness = 0.0;
	
        for(int i = 0; i<population.length; i++)
        {
        	accumulatingFitness += routeFitness[i];
        	
        	if(pointer < accumulatingFitness)
        	{
        		returnInt = i;
        		i = population.length;
        	}
        }
        
        return returnInt;
        
	}
	
	public static void main(String [] args)
	{
		
		createMatrices();
		
		population = new String [MAX_POPULATION][cities.length+1][2];
		intermediatePopulation = new String [MAX_POPULATION][cities.length+1][2];
		//printDistanceMatrices();
		
		initialisePopulation();
		
		//printPopulation();
		
		evaluatePopulation();
		
		printEvaluatedPopulation();
		
		generateNewGeneration();
		
		evaluatePopulation();
		
	}
	
	
	
	/**
	 * Methods below this point are for test purposes.
	 */
	
	//for checking the contents of the distance and cities arrays.
	private static void printDistanceMatrices()
	{
		for(int i = 0; i<cities.length;i++)
		{
			System.out.print(cities[i]+"\t");
		}
		System.out.println();
		for(int i = 0; i<distance.length; i++)
		{
			for(int j = 0; j<distance.length; j++)
			{
				System.out.print(distance[i][j]+"\t");
			}
			System.out.println();
		}
	}
	//for checking the contents of the population array.
	private static void printPopulation()
	{
		for(int i = 0; i<MAX_POPULATION; i++)
		{
			System.out.print(population[i][0][0]);
			for(int j = 0; j<cities.length; j++)
			{
				System.out.print("--(" + population[i][j+1][1] + ")->" + population[i][j+1][0]);
			}
			System.out.println();
		}
	}
	
	private static void printIntermediatePopulation()
	{
		for(int i = 0; i<MAX_POPULATION; i++)
		{
			System.out.print(intermediatePopulation[i][0][0]);
			for(int j = 0; j<cities.length; j++)
			{
				System.out.print("--(" + intermediatePopulation[i][j+1][1] + ")->" + intermediatePopulation[i][j+1][0]);
			}
			System.out.println();
		}
	}
	
	private static void printEvaluatedPopulation()
	{
		System.out.println("--------Generation "+generation+"--------");
	
		for(int i = 0; i<MAX_POPULATION; i++)
		{
			if(i == bestRoute)
			{
				System.out.print("Best so far: ");
			}else
			{
				System.out.print("             ");
			}
			
			for(int j = 0; j<cities.length+1; j++)
			{
				if(j==0)
				{
					System.out.print(population[i][j][0]);
				}else
				{
					System.out.print("-->" + population[i][j][0]);
				}
			}
			System.out.println("         Distance: "+ routeDistance[i] +"         Fitness: "+routeFitness[i]);
		}
	}
}
