package UI;

import api.HotelResource;
import model.IRoom;
import model.Reservation;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Scanner;

public class MainMenu {

    private static final HotelResource hotelResource = HotelResource.getSingleton();

    public static void mainMenu() {
        String line;
        Scanner scanner = new Scanner(System.in);

        printMainMenu();

        try {
            do {
                line = scanner.nextLine();

                if (line.length() == 1) {
                    switch (line.charAt(0)) {
                        case '1' -> findAndReserveRoom();
                        case '2' -> seeMyReservation();
                        case '3' -> createAnAccount(scanner);
                        case '4' -> AdminMenu.adminMenu();
                        case '5' -> System.out.println("Exit");
                        default -> System.out.println("Unknown action\n");
                    }
                } else {
                    System.out.println("Error: Invalid action\n");
                }
            } while (line.charAt(0) != '5' || line.length() != 1);
        } catch (StringIndexOutOfBoundsException ex) {
            System.out.println("Empty input received. Exiting program...");
        }
    }

    private static void findAndReserveRoom() {
        final Scanner scanner = new Scanner(System.in);

        System.out.println("Enter Check-In Date mm/dd/yyyy example 02/01/2020");
        Date checkIn = getValidCheckInDate(scanner);

        if (checkIn == null) {
            System.out.println("Invalid check-in date");
            return;
        }

        System.out.println("Enter Check-Out Date mm/dd/yyyy example 02/21/2020");
        Date checkOut = getValidCheckOutDate(scanner, checkIn);

        if (checkOut == null) {
            System.out.println("Invalid check-out date");
            return;
        }

        Collection<IRoom> availableRooms = hotelResource.findARoom(checkIn, checkOut);

        if (availableRooms.isEmpty()) {
            Collection<IRoom> alternativeRooms = hotelResource.findAlternativeRooms(checkIn, checkOut);

            if (alternativeRooms.isEmpty()) {
                System.out.println("No rooms found.");
                printMainMenu();
            } else {
                final Date alternativeCheckIn = hotelResource.addDefaultPlusDays(checkIn);
                final Date alternativeCheckOut = hotelResource.addDefaultPlusDays(checkOut);
                System.out.println("We've only found rooms on alternative dates:" +
                        "\nCheck-In Date:" + alternativeCheckIn +
                        "\nCheck-Out Date:" + alternativeCheckOut);

                printRooms(alternativeRooms);
                reserveRoom(scanner, alternativeCheckIn, alternativeCheckOut, alternativeRooms);
            }
        } else {
            printRooms(availableRooms);
            reserveRoom(scanner, checkIn, checkOut, availableRooms);
        }
    }


    private static Date getValidCheckInDate(Scanner scanner) {
        SimpleDateFormat DateFor = new SimpleDateFormat("MM/dd/yyyy");
        Calendar calendar = Calendar.getInstance();
        Date checkInDate = null;
        boolean validCheckInDate = false;
        while (!validCheckInDate) {
            System.out.println("Check-in date (mm/dd/yyyy): ");
            String inputCheckInDate = scanner.nextLine();
            try {
                checkInDate = DateFor.parse(inputCheckInDate);
                calendar.setTime(checkInDate);
                if (!calendar.isSet(Calendar.YEAR) || !calendar.isSet(Calendar.MONTH) || !calendar.isSet(Calendar.DAY_OF_MONTH)) {
                    // date is not fully specified
                    System.out.println("Please enter a valid date (mm/dd/yyyy)");
                } else if (checkInDate.before(new Date())) { // check-in date can't be in the past
                    System.out.println("The check-in date cannot be in the past");
                } else if (calendar.get(Calendar.YEAR) < calendar.getMinimum(Calendar.YEAR) || calendar.get(Calendar.YEAR) > calendar.getMaximum(Calendar.YEAR)) {
                    // year is not within valid range
                    System.out.println("Please enter a valid year (YYYY)");
                } else if (calendar.get(Calendar.MONTH) < calendar.getMinimum(Calendar.MONTH) || calendar.get(Calendar.MONTH) > calendar.getMaximum(Calendar.MONTH)) {
                    // month is not within valid range
                    System.out.println("Please enter a valid month (MM)");
                } else if (calendar.get(Calendar.DAY_OF_MONTH) < calendar.getMinimum(Calendar.DAY_OF_MONTH) || calendar.get(Calendar.DAY_OF_MONTH) > calendar.getActualMaximum(Calendar.DAY_OF_MONTH)) {
                    // day is not within valid range for the month
                    System.out.println("Please enter a valid day for the month (DD)");
                } else {
                    validCheckInDate = true;
                }
            } catch (ParseException ex) {
                System.out.println("Invalid date format, please use dd/mm/yyyy");
            }
        }
        return checkInDate;
    }



    private static Date getValidCheckOutDate(Scanner scanner, Date checkInDate) {
        SimpleDateFormat DateFor = new SimpleDateFormat("MM/dd/yyyy");
        Calendar calendar = Calendar.getInstance();
        Date checkOutDate = null;
        boolean validCheckOutDate = false;
        while (!validCheckOutDate) {
            System.out.println("Check-out date (mm/dd/yyyy): ");
            String inputCheckOutDate = scanner.nextLine();
            try {
                checkOutDate = DateFor.parse(inputCheckOutDate);
                calendar.setTime(checkOutDate);
                if (!calendar.isSet(Calendar.YEAR) ||
                        !calendar.isSet(Calendar.MONTH) ||
                        !calendar.isSet(Calendar.DAY_OF_MONTH)) {
                    // date is not fully specified
                    System.out.println("Please enter a valid date (mm/dd/yyyy)");
                } else if (checkOutDate.before(checkInDate)) { // check-out date can't be before the check-in date
                    System.out.println("The check-out date can't be before the check-in date");
                } else if (calendar.get(Calendar.YEAR) < calendar.getMinimum(Calendar.YEAR) || calendar.get(Calendar.YEAR) > calendar.getMaximum(Calendar.YEAR)) {
                    // year is not within valid range
                    System.out.println("Please enter a valid year (YYYY)");
                } else if (calendar.get(Calendar.MONTH) < calendar.getMinimum(Calendar.MONTH) || calendar.get(Calendar.MONTH) > calendar.getMaximum(Calendar.MONTH)) {
                    // month is not within valid range
                    System.out.println("Please enter a valid month (MM)");
                } else if (calendar.get(Calendar.DAY_OF_MONTH) < calendar.getMinimum(Calendar.DAY_OF_MONTH) || calendar.get(Calendar.DAY_OF_MONTH) > calendar.getActualMaximum(Calendar.DAY_OF_MONTH)) {
                    // day is not within valid range for the month
                    System.out.println("Please enter a valid day for the month (DD)");
                } else {
                    validCheckOutDate = true;
                }
            } catch (ParseException ex) {
                System.out.println("Invalid date format, please use dd/mm/yyyy");
            }
        }
        return checkOutDate;
    }


    private static void reserveRoom(final Scanner scanner, final Date checkInDate,
                                    final Date checkOutDate, final Collection<IRoom> rooms) {
        System.out.println("Would you like to book? y/n");
        final String bookRoom = scanner.nextLine();

        if ("y".equals(bookRoom)) {
            System.out.println("Do you have an account with us? y/n");
            final String haveAccount = scanner.nextLine();

            if ("y".equals(haveAccount)) {
                System.out.println("Enter Email format: name@domain.com");
                final String customerEmail = scanner.nextLine();

                if (hotelResource.getCustomer(customerEmail) == null) {
                    System.out.println("Customer not found.\nYou may need to create a new account.");
                } else {
                    System.out.println("What room number would you like to reserve?");
                    final String roomNumber = scanner.nextLine();

                    if (rooms.stream().anyMatch(room -> room.getRoomNumber().equals(roomNumber))) {
                        final IRoom room = hotelResource.getRoom(roomNumber);

                        final Reservation reservation = hotelResource
                                .bookARoom(customerEmail, room, checkInDate, checkOutDate);
                        System.out.println("Reservation created successfully!");
                        System.out.println(reservation);
                    } else {
                        System.out.println("Error: room number not available.\nStart reservation again.");
                    }
                }

                printMainMenu();
            } else {
                System.out.println("Please, create an account.");
                printMainMenu();
            }
        } else if ("n".equals(bookRoom)){
            printMainMenu();
        } else {
            reserveRoom(scanner, checkInDate, checkOutDate, rooms);
        }
    }

    private static void printRooms(final Collection<IRoom> rooms) {
        if (rooms.isEmpty()) {
            System.out.println("No rooms found.");
        } else {
            rooms.forEach(System.out::println);
        }
    }

    private static void seeMyReservation() {
        final Scanner scanner = new Scanner(System.in);

        System.out.println("Enter your Email format: name@domain.com");
        final String customerEmail = scanner.nextLine();

        printReservations(hotelResource.getCustomersReservations(customerEmail));
        printMainMenu();
    }

    private static void printReservations(final Collection<Reservation> reservations) {
        if (reservations == null || reservations.isEmpty()) {
            System.out.println("No reservations found.");
        } else {
            reservations.forEach(reservation -> System.out.println("\n" + reservation));
        }
    }

    private static void createAnAccount(Scanner scanner) {
        System.out.println("First name: ");
        String firstName = scanner.nextLine();
        System.out.println("Last name: ");
        String lastName = scanner.nextLine();
        String email;
        boolean validateEmail = false;
        while(!validateEmail) {
            try {
                System.out.println("Email: ");
                email = scanner.nextLine();
                // Check if account with this email already exists
                if (HotelResource.customerExists(email)) {
                    throw new IllegalArgumentException("An account with this email already exists");
                }
                HotelResource.createACustomer(email, firstName, lastName);
                System.out.println("Account created successfully\n");
                validateEmail = true;
            } catch (IllegalArgumentException ex) {
                System.out.println(ex.getLocalizedMessage());
            }
        }
        printMainMenu();
    }


    public static void printMainMenu()
    {
        System.out.print("""

                Welcome to the Hotel Reservation Application
                --------------------------------------------
                1. Find and reserve a room
                2. See my reservations
                3. Create an Account
                4. Admin
                5. Exit
                --------------------------------------------
                Please select a number for the menu option:
                """);
    }
}
