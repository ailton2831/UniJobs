# UniJobs (UnicvJobs)

Aplicativo Android desenvolvido como projeto da disciplina de **Aplicações Android** na **Universidade de Cabo Verde (Uni-CV)**.

O UniJobs conecta **estudantes universitários** a **empresas**, funcionando como uma plataforma simplificada de vagas de emprego/estágio: empresas publicam oportunidades e estudantes se candidatam anexando o currículo em PDF.

## 📱 Screenshots

<!-- Adicione aqui os prints do app, exemplo: -->
<!-- <img src="screenshots/tela_login.png" width="250"/> <img src="screenshots/lista_vagas.png" width="250"/> -->

## ✨ Funcionalidades

- **Autenticação** de usuários com dois perfis: Aluno (estudante) e Empresa
- **Empresas**: criar e publicar vagas, visualizar candidatos, aprovar/reprovar candidaturas, encerrar vagas
- **Alunos**: navegar pelas vagas disponíveis, candidatar-se anexando currículo em **PDF**, acompanhar status das candidaturas (Pendente / Aprovado / Reprovado)
- Upload e visualização de currículos em PDF via Firebase Storage
- Perfis editáveis para aluno e empresa

## 🛠️ Tecnologias

- **Kotlin** (Android nativo)
- **Firebase Authentication** — autenticação de usuários
- **Firebase Firestore** — banco de dados em tempo real
- **Firebase Storage** — armazenamento dos currículos em PDF
- **Material Design 3** (Material Components for Android)
- **View Binding**

## 🚀 Como rodar o projeto

1. Clone o repositório:
   ```bash
   git clone https://github.com/ailton2831/UniJobs.git
   ```
2. Abra o projeto no **Android Studio**.
3. Crie um projeto no [Firebase Console](https://console.firebase.google.com) e ative:
   - Authentication (método Email/Senha)
   - Firestore Database
   - Storage
4. Baixe o arquivo `google-services.json` do seu projeto Firebase e coloque em `app/google-services.json` (esse arquivo não é versionado no repositório por segurança).
5. Configure as **Regras de Segurança** do Firestore e do Storage para exigir autenticação (ver seção abaixo).
6. Sincronize o Gradle e rode o app em um emulador ou dispositivo físico.

### Regras de segurança recomendadas (Storage)

```
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    match /cvs/{userId}/{arquivo} {
      allow write: if request.auth != null
                   && request.auth.uid == userId
                   && request.resource.size < 5 * 1024 * 1024
                   && request.resource.contentType == 'application/pdf';
      allow read: if request.auth != null;
    }
    match /{allPaths=**} {
      allow read, write: if false;
    }
  }
}
```

## 📂 Estrutura do projeto

```
com.example.unicvjobs
├── admin/          # Dashboard e listagens administrativas
├── autenticacao/    # Login, registo e tela de autenticação
├── classes/         # Data classes (models): Vaga, Candidatura, User, Empresa
├── empresa/         # Telas do perfil empresa: criar vaga, perfil, setup
├── estudante/        # Telas do perfil aluno: candidaturas, perfil, setup
├── vaga/             # Detalhe de vaga
├── adapter/          # Adapters de RecyclerView (Vagas, Candidaturas)
└── splash/           # Tela inicial de carregamento
```

## 👥 Autores

- **Ailton Leniny**
- **Robson Afonso**
- **Nuno Fernandes**

Projeto acadêmico — Universidade de Cabo Verde (Uni-CV), Curso de Engenharia Informática.

## 📄 Licença

Projeto acadêmico sem fins comerciais.
