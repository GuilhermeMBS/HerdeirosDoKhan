import java.util.Scanner;

public class Main {
	public static void main(String[] args) {
		Scanner s = new Scanner(System.in);
		double nota, soma = 0.0;
		
		System.out.println("Informe uma nota: ");
		nota = s.nextDouble();
		
		int i = 0;
		
		for (; nota >= 0.0 && nota <= 10.0; i++) {
			soma += nota;
			System.out.println("Informe uma nota: ");
			nota = s.nextDouble();
		}
		
		s.close();
		System.out.printf("A m2édia da turma foi: %.1f\n", soma/i);
	}
}
