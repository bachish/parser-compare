//
///**
// * ##### # ########### ## ##### ######### ####.
// *
// * @###### (#### ####)
// * @####### (# ####### ###### ## # ####)
// */
//import java.util.ArrayList;
//public class testMusic
//{
//    public static void main(String[] args)
//    {
//        Music [] value = new Music [] {new Music("Pieces of You", 1994, "Jewel"),
//                new Music("Jagged LIttle Pill", 1995, "Alanis Morissette"),
//                new Music("What if it's you", 1995, "Reba McEntire"),
//                new Music("Misunderstood", 2001, "Pink"),
//                new Music("Laundry Service", 2001, "Shakira"),
//                new Music("Taking the long way", 2006, "Dixie Chicks"),
//                new Music("Under my skin", 2004, "Avril Lavigne"),
//                new Music("Let go", 2002, "Avril Lavigne"),
//                new Music("Let it go", 2007, "Tim McGraw"),
//                new Music("White Flag", 2004, "Dido")
//        };
//        searchTitle(value, "Let go");
//        searchTitle(value, "Some day");
//        searchYear(value, 2001);
//        searchYear(value, 2003);
//        searchSinger(value, "Avril Lavigne");
//        searchSinger(value, "Tony Curtis");
//    }
//
//    public static void printMusic(Music [] a)
//    {
//        System.out.println("Title                     Year        Singer");
//        System.out.println("---------------------------------------------------");
//        for(int i = 0; i<a.length;i++)
//        {
//            System.out.println(a[i]);
//        }
//    }
//
//    public static void searchTitle(Music[] r, String Title)
//    {
//        ArrayList <String> found = new ArrayList <String>();
//
//        for(int i = 0; i < r.length; i++)
//            if (r[i].getTitle().equals(Title))
//            {
//                String x = "" + i + "";
//                found.add(x);
//            }
//        if (found != 0)
//        {  // we have found the person
//            System.out.println("\nSearch -  Title - " + Title);
//            System.out.println("Find results: ");
//            System.out.println(r[found]);
//            System.out.println("There were " + found.size() + " listings for " + Title + "\n");
//        }
//        else
//            System.out.println(Title + " is not in the roster");
//    }
//
//    public static void searchYear(Music[] r, int Year)
//    {
//        ArrayList <int> found = new ArrayList <int>();
//
//        for(int i = 0; i < r.length; i++)
//            if (r[i].getYear()==Year)
//            {
//
//                found.add(i);
//            }
//        if (found != 0)
//        {  // we have found the perso
//            System.out.println("\nSearch -  Year - " + Year);
//            System.out.println("Find results: ");
//            System.out.println(r[found]);
//            System.out.println("There were " + found + " listings for " + Year+ "\n");
//        }
//        else
//            System.out.println(Year + " is not in the roster");
//    }
//
//    public static void searchSinger(Music[] r, String Singer)
//    {
//        ArrayList <String> found = new ArrayList <String>();
//
//        for(int i = 0; i < r.length; i++)
//            if (r[i].getSinger().equals(Singer))
//            {
//                ArrayList <String> found = new ArrayList <String>();
//                found.add(i);
//            }
//        if (found != 0)
//        {  // we have found the person
//            System.out.println("\nSearch -  Singer - " + Singer);
//            System.out.println("Find results: ");
//            System.out.println(r[found]);
//            System.out.println("There were " + found.size() + " listings for " + Singer + "\n");
//
//        }
//        else
//            System.out.println(Singer + " is not in the roster");
//    }
//
//
//
//}