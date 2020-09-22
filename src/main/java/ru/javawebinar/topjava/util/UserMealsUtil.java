package ru.javawebinar.topjava.util;

import ru.javawebinar.topjava.model.UserMeal;
import ru.javawebinar.topjava.model.UserMealWithExcess;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.util.*;
import java.util.stream.Collectors;

public class UserMealsUtil {
    public static void main(String[] args) {
        List<UserMeal> meals = Arrays.asList(
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 30, 10, 0), "Завтрак", 500),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 30, 13, 0), "Обед", 1000),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 30, 20, 0), "Ужин", 500),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 31, 0, 0), "Еда на граничное значение", 100),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 31, 10, 0), "Завтрак", 1000),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 31, 13, 0), "Обед", 500),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 31, 20, 0), "Ужин", 410)
        );

        List<UserMealWithExcess> mealsTo = filteredByCycles(meals, LocalTime.of(7, 0), LocalTime.of(12, 0), 2000);
        mealsTo.forEach(System.out::println);

        System.out.println(filteredByStreams(meals, LocalTime.of(7, 0), LocalTime.of(12, 0), 2000));
    }

    public static List<UserMealWithExcess> filteredByCycles(List<UserMeal> meals, LocalTime startTime, LocalTime endTime, int caloriesPerDay) {
        List<UserMealWithExcess> mealsAfterFiltering = new ArrayList<>();
        Map<LocalDate, Integer> caloriesAmountPerDay = new HashMap<>();

        //Counting calories amount for each day.
        for(UserMeal meal : meals){
            LocalDate mealDate = meal.getDateTime().toLocalDate();
            int currentCalories = meal.getCalories();

            if(caloriesAmountPerDay.containsKey(mealDate)) {
                int oldValue = caloriesAmountPerDay.get(mealDate);
                int newValue = oldValue + currentCalories;

                caloriesAmountPerDay.replace(mealDate,oldValue,newValue);
            }
            else
                caloriesAmountPerDay.put(mealDate, currentCalories);
        }

        /*Creating list of meals with excess
          and filtering by time */
        for(UserMeal meal : meals) {

            LocalDateTime dateTime = meal.getDateTime();
            LocalDate date = dateTime.toLocalDate();
            LocalTime time = dateTime.toLocalTime();
            String description = meal.getDescription();
            int calories = meal.getCalories();

            boolean excess = caloriesAmountPerDay.get(date) <= caloriesPerDay;

            UserMealWithExcess mealWithExcess = new UserMealWithExcess(dateTime,description,calories,excess);

            if(time.equals(startTime) || time.isAfter(startTime) && time.isBefore(endTime))
                mealsAfterFiltering.add(mealWithExcess);
        }

            return mealsAfterFiltering;

    }

    public static List<UserMealWithExcess> filteredByStreams(List<UserMeal> meals, LocalTime startTime, LocalTime endTime, int caloriesPerDay) {
        Map<LocalDate, Integer> caloriesAmountPerDay = meals
                .stream()
                .collect(
                        Collectors.toMap(k -> k.getDateTime().toLocalDate(), UserMeal::getCalories, Integer::sum)
                );

        return meals
                .stream()
                .map(meal -> new UserMealWithExcess(
                        meal.getDateTime(),
                        meal.getDescription(),
                        meal.getCalories(),
                        caloriesAmountPerDay.get(meal.getDateTime().toLocalDate())<= caloriesPerDay))
                .filter(meal -> meal.getDateTime().toLocalTime().isAfter(startTime) || meal.getDateTime().toLocalTime().equals(startTime))
                .filter(meal -> meal.getDateTime().toLocalTime().isBefore(endTime))
                .collect(Collectors.toList());
    }
}
