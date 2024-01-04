package com.driver.controllers;

import com.driver.model.Airport;
import com.driver.model.City;
import com.driver.model.Flight;
import com.driver.model.Passenger;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class AirportRepository {

    private Map<String , Airport> airportMap = new HashMap<>();
    private Map<Integer , Flight> flightMap = new HashMap<>();
    private Map<Integer , Passenger> passengerMap = new HashMap<>();

    private Map<Integer , List<Integer>> flightPassengerDetails = new HashMap<>();

    public void addAirport(Airport airport){
        airportMap.put(airport.getAirportName() , airport);
    }


    public String getLargestAirportName(){

        //Largest airport is in terms of terminals. 3 terminal airport is larger than 2 terminal airport
        //Incase of a tie return the Lexicographically smallest airportName
        int largestTerminal = 0;
        for(Airport airport : airportMap.values()){
            if(airport.getNoOfTerminals() > largestTerminal){
                largestTerminal = airport.getNoOfTerminals();
            }
        }
        List<String> airports = new ArrayList<>();
        for(Airport airport : airportMap.values()){
            if(largestTerminal == airport.getNoOfTerminals()){
                airports.add(airport.getAirportName());
            }
        }
        Collections.sort(airports);
        return airports.get(0);
    }


    public double getShortestDurationOfPossibleBetweenTwoCities(City fromCity, City toCity){

        double minTime = Double.MAX_VALUE;
        for(Flight flight : flightMap.values()){
            if(flight.getFromCity() == fromCity && flight.getToCity() == toCity){
                minTime = Math.min(minTime , flight.getDuration());
            }
        }
        return minTime == Double.MAX_VALUE ? -1 : minTime;
    }


    public int getNumberOfPeopleOn(Date date , String airportName){

        //Calculate the total number of people who have flights on that day on a particular airport
        //This includes both the people who have come for a flight and who have landed on an airport after their flight
        int count = 0;
        if(airportMap.containsKey(airportName)){
            City city = airportMap.get(airportName).getCity();
            for(Integer flightId : flightPassengerDetails.keySet()){
                Flight flight = flightMap.get(flightId);
                if(flight.getFlightDate().equals(date) && (flight.getFromCity().equals(city) || flight.getToCity().equals(city))){
                    count += flightPassengerDetails.get(flightId).size();
                }
            }
        }

        return count;
    }


    public int calculateFlightFare(Integer flightId){

        //Calculation of flight prices is a function of number of people who have booked the flight already.
        //Price for any flight will be : 3000 + noOfPeopleWhoHaveAlreadyBooked*50
        //Suppose if 2 people have booked the flight already : the price of flight for the third person will be 3000 + 2*50 = 3100
        //This will not include the current person who is trying to book, he might also be just checking price
        int passengerSize = flightPassengerDetails.get(flightId).size();
        return 3000 + passengerSize * 50;

    }



    public String bookATicket(Integer flightId,Integer passengerId){

        List<Integer> passengers = new ArrayList<>();

        if(flightPassengerDetails.containsKey(flightId)){
            passengers = flightPassengerDetails.get(flightId);

            Flight flight = flightMap.get(flightId);
            //If the numberOfPassengers who have booked the flight is greater than : maxCapacity, in that case //return a String "FAILURE"
            if(passengers.size() > flight.getMaxCapacity()){
                return "FAILURE";
            }

            //Also if the passenger has already booked a flight then also return "FAILURE".
            if(passengers.contains(passengerId)){
                return "FAILURE";
            }
        }
        //else if you are able to book a ticket then return "SUCCESS"
        passengers.add(passengerId);
        flightPassengerDetails.put(flightId , passengers);
        return "SUCCESS";
    }

    public String cancelATicket(Integer flightId,Integer passengerId){

        //If the passenger has not booked a ticket for that flight or the flightId is invalid or in any other failure case
        // then return a "FAILURE" message
        // Otherwise return a "SUCCESS" message
        // and also cancel the ticket that passenger had booked earlier on the given flightId

        if(flightPassengerDetails.containsKey(flightId)){
            List<Integer> passengers = flightPassengerDetails.get(flightId);
            if(passengers == null) return "FAILURE";

            if(passengers.contains(passengerId)){
                passengers.remove(passengerId);
                flightPassengerDetails.put(flightId , passengers);
                return "SUCCESS";
            }
        }
        return "FAILURE";
    }



    public int countOfBookingsDoneByPassengerAllCombined(Integer passengerId){

        //Tell the count of flight bookings done by a passenger: This will tell the total count of flight bookings done by a passenger :
        int count = 0;
        for(List<Integer> passList : flightPassengerDetails.values()){
            if(passList.contains(passengerId)){
                count++;
            }
        }
        return count;
    }


    public void addFlight(Flight flight){
        flightMap.put(flight.getFlightId() , flight);
    }



    public String getAirportNameFromFlightId(Integer flightId){

        //We need to get the starting airportName from where the flight will be taking off (Hint think of City variable if that can be of some use)
        //return null incase the flightId is invalid or you are not able to find the airportName

        Flight flight = flightMap.getOrDefault(flightId , null);
        if(flight == null) return null;

        City city = flight.getFromCity();
        for(Airport airport : airportMap.values()){
            if(city.equals(airport.getCity())){
                return airport.getAirportName();
            }
        }
        return null;
    }



    public int calculateRevenueOfAFlight(Integer flightId){

        //Calculate the total revenue that a flight could have
        //That is of all the passengers that have booked a flight till now and then calculate the revenue
        //Revenue will also decrease if some passenger cancels the flight
        int totalRevenue = 0;
        if(flightPassengerDetails.containsKey(flightId)){
            int count = flightPassengerDetails.get(flightId).size();
            for(int i=0 ; i<count ; i++) {
                totalRevenue += 3000 + i*50;
            }
        }
        return totalRevenue;
    }

    public void addPassenger(Passenger passenger){
        passengerMap.put(passenger.getPassengerId() , passenger);
    }
}
