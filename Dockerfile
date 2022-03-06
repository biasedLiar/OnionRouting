FROM python
RUN mkdir /app
COPY ./hello.py /app
ENTRYPOINT ["python", "/app/hello.py"]
CMD ["world"]