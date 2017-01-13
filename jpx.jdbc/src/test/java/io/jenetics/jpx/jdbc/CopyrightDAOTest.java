/*
 * Java GPX Library (@__identifier__@).
 * Copyright (c) @__year__@ Franz Wilhelmstötter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Author:
 *    Franz Wilhelmstötter (franz.wilhelmstoetter@gmail.com)
 */
package io.jenetics.jpx.jdbc;

import static io.jenetics.jpx.jdbc.Lists.map;

import java.sql.SQLException;
import java.util.List;
import java.util.Random;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.jenetics.jpx.Copyright;
import io.jenetics.jpx.CopyrightTest;

/**
 * @author <a href="mailto:franz.wilhelmstoetter@gmail.com">Franz Wilhelmstötter</a>
 */
public class CopyrightDAOTest extends DAOTestBase<Copyright> {

	@Override
	public Copyright nextObject(final Random random) {
		return CopyrightTest.nextCopyright(random);
	}

	private final List<Copyright> objects = nextObjects(new Random(123), 20);

	@Test
	public void insert() throws SQLException {
		db.transaction(conn -> {
			new CopyrightDAO(conn).insert(objects);
		});
	}

	@Test(dependsOnMethods = "insert")
	public void select() throws SQLException {
		final List<Stored<Copyright>> existing = db.transaction(conn -> {
			return new CopyrightDAO(conn).select();
		});

		Assert.assertEquals(map(existing, Stored::value), objects);
	}

	@Test(dependsOnMethods = "insert")
	public void selectByAuthor() throws SQLException {
		final List<Stored<Copyright>> selected = db.transaction(conn -> {
			return new CopyrightDAO(conn)
				.selectBy("author", objects.get(0).getAuthor());
		});

		Assert.assertEquals(selected.get(0).value(), objects.get(0));
	}

	@Test(dependsOnMethods = "select")
	public void update() throws SQLException {
		final List<Stored<Copyright>> existing = db.transaction(conn -> {
			return new CopyrightDAO(conn).select();
		});

		db.transaction(conn -> {
			final Stored<Copyright> updated = existing.get(0)
				.map(l -> Copyright.of(l.getAuthor(), 2019, (String)null));

			Assert.assertEquals(
				new CopyrightDAO(conn).update(updated),
				updated
			);

			Assert.assertEquals(new CopyrightDAO(conn).select().get(0), updated);
		});
	}

	@Test(dependsOnMethods = "update")
	public void put() throws SQLException {
		db.transaction(conn -> {
			final CopyrightDAO dao = new CopyrightDAO(conn);

			dao.put(objects);
			Assert.assertEquals(map(dao.select(), Stored::value), objects);
		});
	}

	@Test(dependsOnMethods = "put")
	public void delete() throws SQLException {
		db.transaction(conn -> {
			final CopyrightDAO dao = new CopyrightDAO(conn);

			final int count = dao
				.deleteBy(Column.of("author", Copyright::getAuthor), objects.get(0));

			Assert.assertEquals(count, 1);

			Assert.assertEquals(
				map(dao.select(), Stored::value),
				objects.subList(1, objects.size())
			);
		});
	}

}
