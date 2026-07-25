package com.br.devsami.infrastructure.persistence;

import com.br.devsami.model.entity.Product;
import com.br.devsami.model.entity.ProductFeedback;
import com.br.devsami.model.entity.User;
import com.br.devsami.model.enums.Feeling;
import com.br.devsami.model.enums.ProductFeedbackCategory;
import com.br.devsami.model.repository.FeedbackRepository;
import com.br.devsami.model.repository.ProductRepository;
import com.br.devsami.model.repository.UserRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class DatabaseSeeder {

    public static void seed() {
        ProductRepository productRepo = new ProductRepository();
        UserRepository userRepo = new UserRepository();
        FeedbackRepository feedbackRepo = new FeedbackRepository();

        // Se já existirem produtos, não duplica
        if (productRepo.countEntries() > 0 && !feedbackRepo.findAllProductFeedbacks(null, null).isEmpty()) {
            System.out.println("ℹ️ Banco de dados já possui produtos e feedbacks. Seeding ignorado.");
            return;
        }

        System.out.println("🌱 Populando banco de dados com produtos e feedbacks de teste...");

        // Remover constraints antigas do H2 que possam limitar os enums novos
        jakarta.persistence.EntityManager em = HibernateUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.createNativeQuery("ALTER TABLE feedbacks DROP CONSTRAINT IF EXISTS CONSTRAINT_443F").executeUpdate();
            em.getTransaction().commit();
        } catch (Exception ignored) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
        } finally {
            em.close();
        }

        // 1. Garantir um Usuário padrão para vincular aos feedbacks
        User usuario = userRepo.findByCpf("11122233344").orElseGet(() -> {
            User u = new User();
            u.setName("Cliente Exemplo");
            u.setCpf("11122233344");
            u.setBirthDate(LocalDate.of(1995, 5, 10));
            userRepo.save(u);
            return u;
        });

        // 2. Criar Produtos
        Product p1 = new Product("7891000100011", "X-Burguer Artesanal 200g", new java.math.BigDecimal("38.90"));
        productRepo.save(p1);

        Product p2 = new Product("7891000100028", "Pizza Calabresa Especial", new java.math.BigDecimal("58.00"));
        productRepo.save(p2);

        Product p3 = new Product("7891000100035", "Batata Rústica com Cheddar", new java.math.BigDecimal("29.90"));
        productRepo.save(p3);

        Product p4 = new Product("7891000100042", "Suco Natural de Laranja 500ml", new java.math.BigDecimal("12.00"));
        productRepo.save(p4);

        Product p5 = new Product("7891000100059", "Petit Gâteau de Chocolate", new java.math.BigDecimal("24.90"));
        productRepo.save(p5);

        // 3. Cadastrar Feedbacks com notas, texto e categorias pré-definidas
        Object[][] feedbacksData = {
                // X-Burguer Artesanal
                {p1, 5, ProductFeedbackCategory.PRAISE, Feeling.SATISFIED, "Hambúrguer maravilhoso, carne suculenta e pão macio!"},
                {p1, 4, ProductFeedbackCategory.QUALITY, Feeling.SATISFIED, "Muito gostoso o hambúrguer, o molho especial é excelente."},
                {p1, 1, ProductFeedbackCategory.TEMPERATURE, Feeling.DISSATISFIED, "O hambúrguer chegou completamente gelado e o queijo nem derreteu."},
                {p1, 2, ProductFeedbackCategory.PRICE, Feeling.DISSATISFIED, "Achei caro demais pelo tamanho do lanche."},

                // Pizza Calabresa
                {p2, 5, ProductFeedbackCategory.PRAISE, Feeling.SATISFIED, "Pizza crocante, bem recheada e entregue bem quentinha."},
                {p2, 2, ProductFeedbackCategory.PACKAGING, Feeling.DISSATISFIED, "A caixa da pizza veio amassada e o recheio grudou na tampa."},
                {p2, 1, ProductFeedbackCategory.QUALITY, Feeling.DISSATISFIED, "Massa muito dura e a calabresa estava queimada."},

                // Batata Rústica
                {p3, 5, ProductFeedbackCategory.PRAISE, Feeling.SATISFIED, "Batata bem temperada e com muito cheddar!"},
                {p3, 2, ProductFeedbackCategory.PORTION, Feeling.DISSATISFIED, "Porção muito pequena, não dá para compartilhar."},
                {p3, 1, ProductFeedbackCategory.TEMPERATURE, Feeling.DISSATISFIED, "Batata fria e murcha, horrível."},

                // Suco de Laranja
                {p4, 5, ProductFeedbackCategory.PRAISE, Feeling.SATISFIED, "Suco geladinho e feito na hora, muito refrescante."},
                {p4, 2, ProductFeedbackCategory.PRICE, Feeling.DISSATISFIED, "Doze reais em um copinho de suco é um absurdo."},

                // Petit Gâteau
                {p5, 5, ProductFeedbackCategory.QUALITY, Feeling.SATISFIED, "Sobremesa dos deuses, chocolate derretendo com sorvete de baunilha."},
                {p5, 4, ProductFeedbackCategory.PRAISE, Feeling.SATISFIED, "Muito bom petit gâteau!"}
        };

        LocalDateTime baseDate = LocalDateTime.now().minusDays(5);

        for (int i = 0; i < feedbacksData.length; i++) {
            Object[] data = feedbacksData[i];
            Product p = (Product) data[0];
            Integer rating = (Integer) data[1];
            ProductFeedbackCategory category = (ProductFeedbackCategory) data[2];
            Feeling feeling = (Feeling) data[3];
            String text = (String) data[4];

            ProductFeedback pf = new ProductFeedback();
            pf.setUser(usuario);
            pf.setProduct(p);
            pf.setRating(rating);
            pf.setProductCategory(category);
            pf.setFeeling(feeling);
            pf.setText(text);
            pf.setReasoning("Feedback populado via Seeder");

            feedbackRepo.save(pf);
        }

        System.out.println("✅ Seeding de produtos e feedbacks concluído com sucesso!");
    }
}
