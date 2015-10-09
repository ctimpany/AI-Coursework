import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Random;


public class RoundTrip 
{
	
	private static String[] cities;
	private static int[][] distance;
	private static String originCity = "Edinburgh";
	
	private static String[][][] population;
	
	private final static int MAX_POPULATION = 10;
	private final static int MAX_GENERATIONS = 20;
	private final static double MUTATION_RATE = 0.2; //ratio of how many genes within a child will be swapped. between 0 and 1.
	private final static double CROSSOVER_RATE = 0.2; //ratio over which genes from each parent will be distributed within the child. Between 0 and 1.
	private static Random random = new Random();
	
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
	
	public static void main(String [] args)
	{
		createMatrices();
		
		population = new String [MAX_POPULATION][cities.length+1][2];
		
		//printDistanceMatrices();
		
		initialisePopulation();
		
		//printPopulation();
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
			System.out.print(originCity);
			for(int j = 0; j<cities.length; j++)
			{
				System.out.print("--(" + population[i][j+1][1] + ")->" + population[i][j+1][0]);
			}
			System.out.println();
		}
	}
}
