# Proyecto de WordCount con Hadoop

Este proyecto utiliza Hadoop para realizar un conteo de palabras tokenizadas en un dataset de tweets preprocesados. El flujo incluye dos algoritmos de preprocesamiento y una implementación en Hadoop para la tokenización y conteo.

## Requisitos

1. **Java** instalado.
2. **Hadoop** configurado y en ejecución.
3. Archivo `commons-csv-1.10.0.jar` en la carpeta `lib` del proyecto Java.
4. Dataset de tweets a preprocesar.

## Algoritmos Previos

Antes de ejecutar el WordCount Tokenizer, se deben ejecutar los siguientes dos algoritmos en Java:

1. **Diccionario.java**: 
   - Crea un diccionario de palabras omitidas.
2. **PreprocesadorConcurrencia.java**: 
   - Preprocesa el dataset de tweets.

### Archivos Generados

Después de ejecutar estos algoritmos, se generarán tres archivos de texto:

- `diccionarioFiltrado.txt`: Contiene el diccionario de palabras omitidas.
- `tweetsP.txt`: Tweets antes del preprocesamiento.
- `resultadoProcesado.txt`: Tweets después del preprocesamiento.

## Pasos de Ejecución

1. Iniciar sesión en el usuario donde está instalado Hadoop y asegurarse de que todos los servicios estén funcionando.
2. Subir el archivo de `resultadoProcesado.txt` a Linux si no se corrieron los algoritmos en el sistema.
3. Insertar el archivo `resultadoProcesado.txt` en el directorio de Hadoop: `/proyecto_concurrencia/input`.
4. Subir el archivo `NGramCount.java` al proyecto de Hadoop.

## Compilación y Ejecución del Tokenizer

1. Navega a la carpeta del proyecto de Hadoop y crea una carpeta `classes`.
2. Corre los siguientes comandos en la terminal:

   ```bash
   export HADOOP_CLASSPATH=$(hadoop classpath)
   echo $HADOOP_CLASSPATH
   javac -classpath $(hadoop classpath) -d classes/ NGramCount.java
   jar -cvf test.jar -C classes/ .
   ```

3. Una vez compilado, ejecuta el siguiente comando para tokenizar y contar las palabras en el dataset:

   ```bash
   hadoop jar test.jar NGramCount /proyecto_concurrencia/input /proyecto_concurrencia/Output 1
   ```

   El último número indica el tamaño de los grupos de palabras a tokenizar.

## Ver Resultados

Para ver el resultado final, ejecuta el siguiente comando:

```bash
hdfs dfs -cat /proyecto_concurrencia/Output/part-r-00000
```

Este comando mostrará el conteo de las palabras en los tweets preprocesados.

