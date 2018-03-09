import java.util.Scanner;

public class Main
{
    public static void main(String[] args)
    {

        String seis =
                "----------\n" +
                        "--------\n" +
                        "--------\n" +
                        "--X#----\n" +
                        "--X#----\n" +
                        "XXX#---X\n" +
                        "XXX---XX\n" +
                        "XXXXXXXX";
        
        Tetris tetris = new Tetris(seis);
        Scanner sc = new Scanner(System.in);

        String tegevus = "";
        do
        {
            System.out.println("Sisesta tegevus: ");
            tegevus = sc.nextLine();
            seis = tetris.arvutaUusSeis(seis,tegevus);
            tetris.setSeis(seis);
        }
        while(!tegevus.equals("exit")); //Stringide puhul tuleb kasutada .equals, mitte ==
    }
}