package org.domain.initializer;

import org.domain.dao.SeamDAO;
import org.domain.exception.ValidationException;
import org.domain.model.User;
import org.domain.model.Workflow;
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
		seamDao.persist(u);
		
		User u2 = new User("marilia.freire@gmail.com", new BasicPasswordEncryptor().encryptPassword("admin"), "Marília Freire");
		seamDao.persist(u2);
		
		Workflow w = new Workflow(u, "Experimento Test");
		seamDao.persist(w);
	}

}
