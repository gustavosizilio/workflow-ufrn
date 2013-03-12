package org.domain.initializer;

import org.domain.dao.SeamDAO;
import org.domain.exception.ValidationException;
import org.domain.model.User;
import org.domain.model.processDefinition.Workflow;
import org.domain.utils.BasicPasswordEncryptor;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Transactional;

@Name("seed")
@Install(precedence = Install.APPLICATION)
public class Seed {
	@In("seamDao") SeamDAO seamDao;
	
	@Observer("org.jboss.seam.postInitialization")
	@Transactional
	public void popula() throws ValidationException {
		User u = new User("gustavosizilio@gmail.com", new BasicPasswordEncryptor().encryptPassword("admin"), "Gustavo Sizílio Nery");
		persist(u);
		
		User u2 = new User("marilia.freire@gmail.com", new BasicPasswordEncryptor().encryptPassword("admin"), "Marília Freire");
		persist(u2);
		
		User u3 = new User("uirakulesza@gmail.com", new BasicPasswordEncryptor().encryptPassword("admin"), "Uira Kulesza");
		persist(u3);
		
		User u4 = new User("eduardoaranha@dimap.ufrn.br", new BasicPasswordEncryptor().encryptPassword("admin"), "Eduardo Aranha");
		persist(u4);
		
		createUsers(20);
		
		Workflow w = new Workflow(u, "Experimento Test");
		persist(w);
	}

	private void createUsers(int qnt) {
		for (int i = 1; i <= qnt; i++) {
			User u = new User("user"+i+"@gmail.com", new BasicPasswordEncryptor().encryptPassword("user"), "User "+i);
			persist(u);
		}
	}

	private void persist(Object u) {
		if(seamDao.findByExample(u).size() == 0){
			seamDao.persist(u);
		}
	}

}
