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
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.metadata.QAuthenticationType;
import com.kingsrook.qqq.backend.core.model.metadata.QBackendMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.authentication.QAuthenticationMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.branding.QBrandingMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.layout.QAppMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.layout.QAppSection;
import com.kingsrook.qqq.backend.core.model.metadata.layout.QIcon;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import com.kingsrook.qqq.backend.module.rdbms.model.metadata.RDBMSBackendMetaData;
import com.kingsrook.qqq.backend.module.rdbms.model.metadata.RDBMSTableBackendDetails;


/*******************************************************************************
 ** Define the qqq meta-data for your application
 *******************************************************************************/
public class StarterAppMetaDataProvider
{
   private static final QLogger LOG = QLogger.getLogger(StarterAppMetaDataProvider.class);

   public static final String RDBMS_BACKEND_NAME = "rdbmsBackend";



   /*******************************************************************************
    ** top-level entry point method, which defines the QInstance, and places all
    ** other meta-data objects inside it.
    *******************************************************************************/
   public static QInstance defineInstance()
   {
      QInstance qInstance = new QInstance();

      qInstance.setBranding(defineBranding());
      qInstance.setAuthentication(defineAuthentication());
      qInstance.addBackend(defineRDBMSBackend());
      qInstance.addTable(defineSampleTable());
      qInstance.addApp(defineSampleApp(qInstance));

      return (qInstance);
   }



   /*******************************************************************************
    ** define the branding meta-data for the app
    *******************************************************************************/
   private static QBrandingMetaData defineBranding()
   {
      return new QBrandingMetaData()
         .withIcon("/sample-app-icon.png")
         .withLogo("/sample-app-logo.png")
         .withCompanyName("Starter App");
   }



   /*******************************************************************************
    ** define the authentication module meta-data for the app
    *******************************************************************************/
   private static QAuthenticationMetaData defineAuthentication()
   {
      LOG.warn("Using fully anonymous authentication module.  This module works for development, but should not be used for any production application!");
      return (new QAuthenticationMetaData()
         .withName("anonymous")
         .withType(QAuthenticationType.FULLY_ANONYMOUS));
   }



   /*******************************************************************************
    ** define an RDBMS (database) backend for teh app
    *******************************************************************************/
   public static QBackendMetaData defineRDBMSBackend()
   {
      //////////////////////////////////////////////////////////////////////////////////
      // this object will parse ${env.XXX} strings and replace them with values       //
      // from a .env file (in the working directory) - or - the process's environment //
      //////////////////////////////////////////////////////////////////////////////////
      QMetaDataVariableInterpreter interpreter = new QMetaDataVariableInterpreter();

      String vendor       = interpreter.interpret("${env.RDBMS_VENDOR}");
      String hostname     = interpreter.interpret("${env.RDBMS_HOSTNAME}");
      String databaseName = interpreter.interpret("${env.RDBMS_DATABASE_NAME}");

      ////////////////////////////////////////////////////////
      // check that at least some of the basic vars are set //
      ////////////////////////////////////////////////////////
      if(!StringUtils.hasContent(vendor) || !StringUtils.hasContent(hostname) || !StringUtils.hasContent(databaseName))
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

      Integer port     = Integer.valueOf(interpreter.interpret("${env.RDBMS_PORT}"));
      String  username = interpreter.interpret("${env.RDBMS_USERNAME}");
      String  password = interpreter.interpret("${env.RDBMS_PASSWORD}");

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
    ** define meta-data for a table
    *******************************************************************************/
   public static QTableMetaData defineSampleTable()
   {
      return new QTableMetaData()
         .withName("sampleTable")
         .withIcon(new QIcon().withName("star"))
         .withPrimaryKeyField("id")
         .withRecordLabelFormat("%s")
         .withRecordLabelFields("name")
         .withBackendName(RDBMS_BACKEND_NAME)
         .withBackendDetails(new RDBMSTableBackendDetails()
            /////////////////////////////////////////////////////////////////////////////////////////////////
            // note - this is the table name within the RDBMS -- not the name that QQQ uses for the table. //
            /////////////////////////////////////////////////////////////////////////////////////////////////
            .withTableName("sample"))
         .withField(new QFieldMetaData("id", QFieldType.INTEGER).withIsEditable(false))
         .withField(new QFieldMetaData("name", QFieldType.STRING));
   }



   /*******************************************************************************
    ** define meta-data for an app - that is - an object which shows up in the UI's
    ** navigation, and which contains other objects (tables, processes).
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
