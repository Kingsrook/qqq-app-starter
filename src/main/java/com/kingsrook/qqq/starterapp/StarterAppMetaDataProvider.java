/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2022.  Kingsrook, LLC
 * 651 N Broad St Ste 205 # 6917 | Middletown DE 19709 | United States
 * contact@kingsrook.com
 * https://github.com/Kingsrook/
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.kingsrook.qqq.starterapp;


import com.kingsrook.qqq.backend.core.instances.QMetaDataVariableInterpreter;
import com.kingsrook.qqq.backend.core.model.metadata.QAuthenticationType;
import com.kingsrook.qqq.backend.core.model.metadata.QBackendMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.layout.QAppMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.layout.QAppSection;
import com.kingsrook.qqq.backend.core.model.metadata.layout.QIcon;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.modules.authentication.metadata.QAuthenticationMetaData;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import com.kingsrook.qqq.backend.module.rdbms.model.metadata.RDBMSBackendMetaData;
import com.kingsrook.qqq.backend.module.rdbms.model.metadata.RDBMSTableBackendDetails;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/*******************************************************************************
 **
 *******************************************************************************/
public class StarterAppMetaDataProvider
{
   private static final Logger LOG = LogManager.getLogger(StarterAppMetaDataProvider.class);

   public static final String RDBMS_BACKEND_NAME = "rdbmsBackend";



   /*******************************************************************************
    **
    *******************************************************************************/
   public static QInstance defineInstance()
   {
      QInstance qInstance = new QInstance();

      qInstance.setAuthentication(defineAuthentication());
      qInstance.addBackend(defineRDBMSBackend());
      qInstance.addTable(defineSampleTable());
      qInstance.addApp(defineSampleApp(qInstance));

      return (qInstance);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static QAuthenticationMetaData defineAuthentication()
   {
      LOG.warn("Using fully anonymous authentication module.  This module works for development, but should not be used for any production application!");
      return (new QAuthenticationMetaData()
         .withName("anonymous")
         .withType(QAuthenticationType.FULLY_ANONYMOUS));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static QBackendMetaData defineRDBMSBackend()
   {
      QMetaDataVariableInterpreter interpreter = new QMetaDataVariableInterpreter();

      String vendor   = interpreter.interpret("${env.RDBMS_VENDOR}");
      String hostname = interpreter.interpret("${env.RDBMS_HOSTNAME}");

      if(!StringUtils.hasContent(vendor) || !StringUtils.hasContent(hostname))
      {
         throw new IllegalStateException("""
            Missing at least one env. var to define RDBMS backend meta data.
            Environment (or .env file) must contain:
            RDBMS_VENDOR
            RDBMS_HOSTNAME
            RDBMS_PORT
            RDBMS_DATABASE_NAME
            RDBMS_USERNAME
            RDBMS_PASSWORD""");
      }

      Integer port         = Integer.valueOf(interpreter.interpret("${env.RDBMS_PORT}"));
      String  databaseName = interpreter.interpret("${env.RDBMS_DATABASE_NAME}");
      String  username     = interpreter.interpret("${env.RDBMS_USERNAME}");
      String  password     = interpreter.interpret("${env.RDBMS_PASSWORD}");

      return new RDBMSBackendMetaData()
         .withName(RDBMS_BACKEND_NAME)
         .withVendor(vendor)
         .withHostName(hostname)
         .withPort(port)
         .withDatabaseName(databaseName)
         .withUsername(username)
         .withPassword(password);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static QTableMetaData defineSampleTable()
   {
      return new QTableMetaData()
         .withName("sample")
         .withIcon(new QIcon().withName("star"))
         .withPrimaryKeyField("id")
         .withRecordLabelFormat("%s")
         .withRecordLabelFields("name")
         .withBackendName(RDBMS_BACKEND_NAME)
         .withBackendDetails(new RDBMSTableBackendDetails()
            .withTableName("sample"))
         .withField(new QFieldMetaData("id", QFieldType.INTEGER).withIsEditable(false))
         .withField(new QFieldMetaData("name", QFieldType.STRING));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static QAppMetaData defineSampleApp(QInstance qInstance)
   {
      return new QAppMetaData()
         .withName("sampleApp")
         .withIcon(new QIcon().withName("star"))
         .withSectionOfChildren(new QAppSection()
               .withName("sampleSection")
               .withIcon(new QIcon().withName("star")),
            qInstance.getTable("sample"));
   }

}
