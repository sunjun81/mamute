package br.com.caelum.brutal.dao;

import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.joda.time.DateTime;

import br.com.caelum.brutal.model.Comment;
import br.com.caelum.brutal.model.SubscribableDTO;
import br.com.caelum.brutal.model.interfaces.Commentable;
import br.com.caelum.vraptor.ioc.Component;

@Component
@SuppressWarnings("rawtypes")
public class CommentDAO {

	private final Session session;

	public CommentDAO(Session session) {
		this.session = session;
	}

	public Commentable load(Class type, Long id) {
		return (Commentable) session.load(type, id);
	}

	public void save(Comment comment) {
		session.save(comment);
	}
	
	@SuppressWarnings("unchecked")
    public List<SubscribableDTO> getSubscribablesAfter(DateTime timeAgo) {
        Query query = session.createQuery("select distinct new br.com.caelum.brutal.model.SubscribableDTO(comment, author, question) from Question question " +
                "join question.comments.comments comment " +
                "join question.answers answer " +
                "join answer.author author " +
                "where (comment.createdAt) > :timeAgo and author != comment.author and author.isSubscribed=true");
        List<SubscribableDTO> results = query.setParameter("timeAgo", timeAgo).list();
        
        query = session.createQuery("select distinct new br.com.caelum.brutal.model.SubscribableDTO(comment, author, question) from Question question " +
        		"join question.author author " +
        		"join question.comments.comments comment " +
        		"join comment.author comment_author " +
        		"where (comment.createdAt) > :timeAgo and author != comment_author and author.isSubscribed=true");
        results.addAll(query.setParameter("timeAgo", timeAgo).list());
        
        query = session.createQuery("select distinct new br.com.caelum.brutal.model.SubscribableDTO(comment, author, question) from Question question " +
        		"join question.author author " +
        		"join question.answers answer " +
        		"join answer.comments.comments comment "+
        		"where (comment.createdAt) > :timeAgo and author != comment.author and author.isSubscribed=true");
        results.addAll(query.setParameter("timeAgo", timeAgo).list());
        
        query = session.createQuery("select distinct new br.com.caelum.brutal.model.SubscribableDTO(comment, author, question) from Question question " +
        		"join question.answers answer " +
        		"join answer.author author "+
        		"join answer.comments.comments comment "+
        		"where (comment.createdAt) > :timeAgo and author != comment.author and author.isSubscribed=true");
        results.addAll(query.setParameter("timeAgo", timeAgo).list());
        
        return results;
    }

	public Comment getById(Long id) {
		return (Comment) session.load(Comment.class, id);
	}

	public List<Comment> flagged(Long minFlagCount) {
		String hql = "select comment from Comment comment left join comment.flags flags group by comment having count(flags) >= :min";
		Query query = session.createQuery(hql);
		return query.setParameter("min", minFlagCount).list();
	}

}
